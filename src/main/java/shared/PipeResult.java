package shared;

public sealed interface PipeResult<T> 
	permits Success, Failure{
}
