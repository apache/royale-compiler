package codegraph.golden
{
    [Event(name="complete", type="codegraph.golden.GraphEvent")]
    [DefaultProperty("label")]
    /**
    * "Root" graph description.
     *
     * @see codegraph.golden.GraphBase
     * @copy codegraph.golden.GraphBase#label
     * @see codegraph.golden.IGraphContract
     */
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