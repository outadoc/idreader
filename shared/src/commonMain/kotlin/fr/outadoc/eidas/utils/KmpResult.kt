package fr.outadoc.eidas.utils

class KmpResult<out T> private constructor(
    private val delegate: Result<T>,
) {
    val isSuccess: Boolean get() = delegate.isSuccess
    val isFailure: Boolean get() = delegate.isFailure

    fun getOrNull(): T? = delegate.getOrNull()
    fun getOrThrow(): T = delegate.getOrThrow()
    fun exceptionOrNull(): Throwable? = delegate.exceptionOrNull()

    fun getOrElse(onFailure: (exception: Throwable) -> @UnsafeVariance T): T = delegate.getOrElse(onFailure)

    fun onSuccess(action: (value: T) -> Unit): KmpResult<T> {
        delegate.onSuccess(action)
        return this
    }

    fun onFailure(action: (exception: Throwable) -> Unit): KmpResult<T> {
        delegate.onFailure(action)
        return this
    }

    fun unwrap(): Result<T> = delegate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as KmpResult<*>
        return delegate == other.delegate
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun toString(): String = "KmpResult($delegate)"

    companion object {
        fun <T> success(value: T): KmpResult<T> = KmpResult(Result.success(value))

        fun <T> failure(exception: Throwable): KmpResult<T> = KmpResult(Result.failure(exception))

        fun <T> Result<T>.wrap(): KmpResult<T> = KmpResult(this)
    }
}
