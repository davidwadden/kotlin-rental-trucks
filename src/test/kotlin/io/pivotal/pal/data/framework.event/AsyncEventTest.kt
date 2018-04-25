package io.pivotal.pal.data.framework.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.Duration.ONE_SECOND
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsyncEventTest {

    private var data: String? = null

    private val publisher: DefaultAsyncEventPublisher<String> = DefaultAsyncEventPublisher(EVENT_NAME)
    private val subscriber: AsyncEventSubscriberAdapter<String> = AsyncEventSubscriberAdapter(EVENT_NAME, Handler())

    @BeforeEach
    fun setUp() {
        data = null
    }

    @Test
    fun `should publish an event asynchronously to the subscriber`() {
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
