package io.pivotal.pal.data.framework.event

import ch.tutteli.atrium.api.cc.en_UK.isNull
import ch.tutteli.atrium.api.cc.en_UK.toBe
import ch.tutteli.atrium.api.cc.en_UK.toThrow
import ch.tutteli.atrium.verbs.assertthat.assertThat
import ch.tutteli.atrium.verbs.expect.expect
import org.awaitility.Awaitility.await
import org.awaitility.Duration.ONE_SECOND
import org.awaitility.Duration.TWO_SECONDS
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AsyncEventTest {

    private var data: String? = null
    private var errorData: String? = null

    @BeforeEach
    fun setUp() {
        data = null
        errorData = null
    }

    @Test
    fun `errors when maxRetryCount is invalid`() {
        expect {
            AsyncEventSubscriberAdapter(EVENT_NAME,
                    ExceptionThrowingHandler(1), null, -1, 100, 2, null)
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun `errors when retry waitTime is invalid`() {
        expect {
            AsyncEventSubscriberAdapter(EVENT_NAME,
                    ExceptionThrowingHandler(1), null, 1, 10, 2, null)
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun `errors when retryWaitTimeMultiplier is invalid`() {
        expect {
            AsyncEventSubscriberAdapter(EVENT_NAME,
                    ExceptionThrowingHandler(1), null, 1, 100, 0, null)
        }.toThrow<IllegalArgumentException>()
    }

    @Test
    fun `publishes an event asynchronously to the subscriber`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME, Handler())
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(ONE_SECOND)
                .untilAsserted { assertThat(data!!).toBe(someData) }
    }

    @Test
    fun `invokes the error handler upon error without retry`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME, ExceptionThrowingHandler(1), ErrorHandler())
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(ONE_SECOND)
                .untilAsserted {
                    assertThat(errorData!!).toBe(someData)
                    assertThat(data).isNull()
                }
    }

    @Test
    fun `succeeds without error handler with retry enabled`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME,
                ExceptionThrowingHandler(1),
                null, 1, 100, 2, null)
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(TWO_SECONDS)
                .untilAsserted {
                    assertThat(errorData).isNull()
                    assertThat(data!!).toBe(someData)
                }
    }

    @Disabled
    @Test
    fun `succeeds without error handler with retry and recoverable exceptions`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME,
                ExceptionThrowingHandler(1), null, 1, 100,
                2, HashSet(listOf(IllegalStateException::class.java)))
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(TWO_SECONDS)
                .untilAsserted {
                    assertThat(errorData).isNull()
                    assertThat(data!!).toBe(someData)
                }
    }

    @Disabled
    @Test
    fun `errors without handler with retry and non-recoverable exceptions`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME,
                ExceptionThrowingHandler(1),
                null, 1, 100,
                2, HashSet(listOf(IllegalStateException::class.java)))
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(TWO_SECONDS)
                .untilAsserted {
                    assertThat(errorData).isNull()
                    assertThat(data).isNull()
                }
    }

    @Test
    fun `succeeds without error handler when retry exceeded`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME,
                ExceptionThrowingHandler(2),
                null, 1, 100, 2, null)
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(TWO_SECONDS)
                .untilAsserted {
                    assertThat(errorData).isNull()
                    assertThat(data).isNull()
                }
    }

    @Test
    fun `returns error without error handler or retry config`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME,
                ExceptionThrowingHandler(1), null, 0, 0, 0, null)
        subscriber.start()

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(ONE_SECOND)
                .untilAsserted {
                    assertThat(errorData).isNull()
                    assertThat(data).isNull()
                }
    }

    private inner class Handler : AsyncEventHandler<String> {

        override fun onEvent(data: String) {
            this@AsyncEventTest.data = data
        }
    }

    private inner class ErrorHandler : AsyncEventHandler<String> {

        override fun onEvent(data: String) {
            this@AsyncEventTest.errorData = data
        }
    }

    private inner class ExceptionThrowingHandler(internal var maxCount: Int) : AsyncEventHandler<String> {
        internal var count = 0

        override fun onEvent(data: String) {
            if (count++ < maxCount) {
                throw IllegalArgumentException("data = $data")
            } else {
                this@AsyncEventTest.data = data
            }
        }
    }

    companion object {

        private const val EVENT_NAME = "test"
    }
}
