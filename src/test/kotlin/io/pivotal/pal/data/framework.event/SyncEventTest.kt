package io.pivotal.pal.data.framework.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class SyncEventTest {

    private lateinit var publisher: DefaultSyncEventPublisher<String, String>
    private lateinit var handler: Handler
    private lateinit var subscriber: SyncEventSubscriberAdapter<String, String>

    @Before
    fun setUp() {
        publisher = DefaultSyncEventPublisher(EVENT_NAME)
        handler = Handler()
        subscriber = SyncEventSubscriberAdapter(EVENT_NAME, handler)

        handler.data = null
    }

    @Test
    fun basicTest() {
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
