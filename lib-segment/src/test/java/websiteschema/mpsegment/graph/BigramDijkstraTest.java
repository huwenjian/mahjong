package websiteschema.mpsegment.graph;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import websiteschema.mpsegment.dict.IWord;
import websiteschema.mpsegment.dict.WordImpl;

@Ignore
public class BigramDijkstraTest {

    private WordBigram wordBigram;

    public BigramDijkstraTest() {
        wordBigram = new WordBigram("word-bigram.dat");
    }

    @Test
    public void should_return_edge_object_according_tail() {
        BigramDijkstra dijkstra = new BigramDijkstra(wordBigram);
        dijkstra.setGraph(createGraph());
        dijkstra.getShortestPath(0, 4);
        IWord word1 = dijkstra.getEdgeObject(3);
        Assert.assertEquals("S2S3", word1.getWordName());
        IWord word2 = dijkstra.getEdgeObject(1);
        Assert.assertEquals("S1", word2.getWordName());
        IWord word3 = dijkstra.getEdgeObject(2);
        Assert.assertEquals("S2", word3.getWordName());
    }

    @Test
    public void should_get_bigram_data() {
        BigramDijkstra dijkstra = new BigramDijkstra(wordBigram);
        double prob = dijkstra.getConditionProbability("好", "地");
        Assert.assertTrue(prob > -0.00000001);
    }

    private IGraph createGraph() {
        IGraph graph = new Graph();
        graph.addEdge(0, 1, 1, new WordImpl("S1"));
        graph.addEdge(0, 2, 5, new WordImpl("S1S2"));
        graph.addEdge(1, 2, 2, new WordImpl("S2"));
        graph.addEdge(1, 3, 2, new WordImpl("S2S3"));
        graph.addEdge(2, 3, 3, new WordImpl("S3"));
        graph.addEdge(0, 3, 5, new WordImpl("S1S2S3"));
        graph.addEdge(3, 4, 1, new WordImpl("S1S2S3"));
        return graph;
    }

}
