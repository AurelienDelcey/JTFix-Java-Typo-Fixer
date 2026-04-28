package shared;

public record Failure<T> (String error) implements PipeResult<T> {

}
