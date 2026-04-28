package shared;

public record Success<T> (T payload) implements PipeResult<T> {

}
