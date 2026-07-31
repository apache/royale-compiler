package codegraph.golden
{
    public class GraphBase
    {
        [Bindable(event="labelChanged")]
        public var label:String;

        public function inheritedMethod():void
        {
        }
    }
}