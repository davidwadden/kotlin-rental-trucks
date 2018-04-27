package io.pivotal.pal.data.framework.event

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.awaitility.Duration.ONE_SECOND
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance


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
    fun `publishes an event asynchronously to the subscriber`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME, Handler())

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(ONE_SECOND)
                .untilAsserted { assertThat(data).isEqualTo(someData) }
    }

    @Test
    fun `invokes the error handler upon error`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME, ExceptionThrowingHandler(), ErrorHandler())

        val someData = "some-data"
        publisher.publish(someData)

        await()
                .atMost(ONE_SECOND)
                .untilAsserted {
                    assertThat(errorData).isEqualTo(someData)
                    assertThat(data).isNull()
                }
    }

    @Test
    fun `handles an error gracefully without an error handler`() {
        val publisher = DefaultAsyncEventPublisher<String>(EVENT_NAME)
        val subscriber = AsyncEventSubscriberAdapter(EVENT_NAME, ExceptionThrowingHandler())

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

    private inner class ExceptionThrowingHandler : AsyncEventHandler<String> {

        override fun onEvent(data: String) {
            throw IllegalArgumentException("data = $data")
        }
    }

    companion object {

        private const val EVENT_NAME = "test"
    }
}
