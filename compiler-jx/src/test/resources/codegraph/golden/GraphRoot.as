package codegraph.golden
{
    public class GraphRoot extends GraphBase implements IGraphContract
    {
        public function GraphRoot(value:String)
        {
        }

        public function execute(required:String, optional:Number = 2, ...rest):Boolean
        {
            return true;
        }

        private function hidden():void
        {
        }
    }
}