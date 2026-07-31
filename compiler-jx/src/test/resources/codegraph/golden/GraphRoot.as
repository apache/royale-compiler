package codegraph.golden
{
    /** Dispatched when graph work completes. */
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
        public static const VERSION:String = "1";

        public function GraphRoot(value:String)
        {
        }

        public function execute(required:String, optional:Number = 2, ...rest):Boolean
        {
            return packageFunction(required);
        }

        override public function inheritedMethod():void
        {
        }

        private function hidden():void
        {
        }

        /**
         * @private
         */
        public function documentedPrivate():void
        {
        }
    }
}