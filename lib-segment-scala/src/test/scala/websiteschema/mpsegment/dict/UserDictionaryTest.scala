//package websiteschema.mpsegment.dict
//
//import org.junit.Assert
//import org.junit.Test
//import websiteschema.mpsegment.core.SegmentEngine
//import websiteschema.mpsegment.core.SegmentResult
//import websiteschema.mpsegment.core.SegmentWorker
//import websiteschema.mpsegment.dict.domain.DomainDictFactory
//import websiteschema.mpsegment.dict.domain.DomainDictionary
//
//class UserDictionaryTest {
//
//    @Test
//    def should_Loaded_Some_Words_from_User_Dictionary() {
//        var str = "贝因美是中国品牌"
//        var engine = SegmentEngine.getInstance()
//        var worker = engine.getSegmentWorker()
//        worker.setUseDomainDictionary(true)
//        var words = worker.segment(str)
//        println(words)
//        Assert.assertEquals(words.length(), 4)
//        Assert.assertEquals(words.getWord(0), "贝因美")
//        Assert.assertEquals(words.getWord(1), "是")
//        var dd = DomainDictFactory.getInstance().getDomainDictionary()
//
//        assert (dd.iterator().hasNext())
//    }
//}