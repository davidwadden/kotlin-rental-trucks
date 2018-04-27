package io.pivotal.pal.data.framework.event

import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.api.cc.en_UK.toThrow
import ch.tutteli.atrium.verbs.assertthat.assertThat
import ch.tutteli.atrium.verbs.expect.expect
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SyncEventTest {

    @Test
    fun `errors when maxRetryCount is invalid`() {
        expect {
            SyncEventSubscriberAdapter(EVENT_NAME,
                    ExceptionThrowingHandler(1),
                    null, -1, 100, 2, null)
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun `errors when retry waitTime is invalid`() {
        expect {
            SyncEventSubscriberAdapter(EVENT_NAME,
                    ExceptionThrowingHandler(1),
                    null, 1, 10, 2, null)
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun `errors when retryWaitTimeMultiplier is invalid`() {
        expect {
            SyncEventSubscriberAdapter(EVENT_NAME,
                    ExceptionThrowingHandler(1),
                    null, 1, 100, 0, null)
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun `succeeds without retry`() {
        val publisher = DefaultSyncEventPublisher<String, String>(EVENT_NAME)
        val handler = Handler()
        val subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler)

        handler.data = null

        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(handler.data!!).toBe(someData)
        assertThat(result!!).toBe(someData)
    }

    @Test
    fun `succeeds with retry`() {
        val publisher = DefaultSyncEventPublisher<String, String>(EVENT_NAME)
        val handler = ExceptionThrowingHandler(1)
        val subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler, null, 1, 100, 2, null)

        handler.data = null

        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(handler.data!!).toBe(someData)
        assertThat(result!!).toBe(someData)
    }

    @Test
    fun `succeeds with retry and recoverable exceptions`() {
        val publisher = DefaultSyncEventPublisher<String, String>(EVENT_NAME)
        val handler = ExceptionThrowingHandler(1)
        val subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler, null, 1, 100, 2, HashSet(Arrays.asList(IllegalArgumentException::class.java)))

        handler.data = null

        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(handler.data!!).toBe(someData)
        assertThat(result!!).toBe(someData)
    }

    @Test
    fun `errors with retry and non-recoverabl exceptions`() {
        val publisher = DefaultSyncEventPublisher<String, String>(EVENT_NAME)
        val handler = ExceptionThrowingHandler(1)
        val subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler, null, 1, 100, 2, HashSet(Arrays.asList(IllegalStateException::class.java)))

        handler.data = null

        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(handler.data).isNull()
        assertThat(result).isNull()
    }

    @Test
    fun `errors with error handler and no retry`() {
        val publisher = DefaultSyncEventPublisher<String, String>(EVENT_NAME)
        val handler = ExceptionThrowingHandler(1)
        val errorHandler = Handler()
        val subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler, errorHandler)

        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(errorHandler.data!!).toBe(someData)
        assertThat(result!!).toBe(someData)
    }

    @Test
    fun `errors with no error handler and no retry`() {
        val publisher = DefaultSyncEventPublisher<String, String>(EVENT_NAME)
        val handler = ExceptionThrowingHandler(1)
        val subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler)

        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(result).isNull()
    }

    private inner class ExceptionThrowingHandler(internal var maxCount: Int) : SyncEventHandler<String, String> {
        internal var count = 0
        var data: String? = null

        override fun onEvent(data: String): String? {
            if (count++ < maxCount) {
                throw IllegalArgumentException("data = $data")
            } else {
                this.data = data
                return data
            }
        }
    }

    private inner class Handler : SyncEventHandler<String, String> {

        var data: String? = null

        override fun onEvent(data: String): String? {
            this.data = data
            return data
        }
    }

    companion object {

        private const val EVENT_NAME = "test"
    }
}
