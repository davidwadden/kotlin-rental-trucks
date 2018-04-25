package io.pivotal.pal.data.framework.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.Duration.ONE_SECOND
import org.junit.Before
import org.junit.Test

class AsyncEventTest {

    private var data: String? = null

    private lateinit var publisher: DefaultAsyncEventPublisher<String>
    private lateinit var subscriber: AsyncEventSubscriberAdapter<String>

    @Before
    fun setUp() {
        publisher = DefaultAsyncEventPublisher(EVENT_NAME)
        subscriber = AsyncEventSubscriberAdapter(EVENT_NAME, Handler())

        data = null
    }

    @Test
    fun basicTest() {
        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(ONE_SECOND)
                .untilAsserted { assertThat(data).isEqualTo(someData) }
    }

    private inner class Handler : AsyncEventHandler<String> {

        override fun onEvent(data: String) {
            this@AsyncEventTest.data = data
        }
    }

    companion object {

        private const val EVENT_NAME = "test"
    }
}
