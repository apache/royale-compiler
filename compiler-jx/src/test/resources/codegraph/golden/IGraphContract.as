package codegraph.golden
{
    public interface IGraphContract
    {
        function execute(required:String, optional:Number = 2, ...rest):Boolean;
    }
}