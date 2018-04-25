package io.pivotal.pal.data.framework.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SyncEventTest {

    private val publisher: DefaultSyncEventPublisher<String, String> = DefaultSyncEventPublisher(EVENT_NAME)
    private val handler: Handler = Handler()
    private val subscriber: SyncEventSubscriberAdapter<String, String> = SyncEventSubscriberAdapter(EVENT_NAME, handler)

    @BeforeEach
    fun setUp() {
        handler.data = null
    }

    @Test
    fun `should publish a message and return with return value`() {
        val someData = "some-data"
        val result = publisher.publish(someData)

        assertThat(handler.data).isEqualTo(someData)
        assertThat(result).isEqualTo(someData)
    }

    private inner class Handler : SyncEventHandler<String, String> {

        var data: String? = null

        override fun onEvent(data: String): String {
            this.data = data
            return data
        }
    }

    companion object {

        private const val EVENT_NAME = "test"
    }
}
