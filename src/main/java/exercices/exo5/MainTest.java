package exercices.exo5;

import com.github.javaparser.utils.SourceRoot;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Graph;
import guru.nidi.graphviz.model.Node;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;

public class MainTest {

    public static void generateGraphs(Map<String, List<PairM>> data) {
        // generate Graph using GraphViz

        // create graphs
        List<Node> nodes = new ArrayList<>();
        data.forEach((k, value) -> {
            nodes.clear();
            value.forEach(v -> {
                nodes.add(node(v.getNode1()).link(to(node(v.getNode2())).with(Label.of(v.getLabel()))));
            });
            Graph g = graph(k).with(nodes);
            try {
                Graphviz.fromGraph(g).render(Format.PNG).toFile(new File("Graphs/"+k+"Graph.png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void generateHistogram(double[] data, String nameProject) throws IOException {
        HistogramDataset histogramDataset = new HistogramDataset();
/*
        double[] values = data.stream().mapToDouble(Double::doubleValue).toArray();
*/
        histogramDataset.addSeries("TCC Values", data, 25);

        // Create a chart
        JFreeChart chart = ChartFactory.createHistogram(
                nameProject + " histogram",
                "TCC values",
                "Fréquence",
                histogramDataset);

        ChartUtils.saveChartAsJPEG(new File("Histogram/histogram.jpeg"), chart, 600, 400);
    }

    public static void main(String[] args) throws IOException {
        if(args.length == 0) {
            System.err.println("Should provide the path to the source code");
            System.exit(1);
        }

        File file = new File(args[0]);
        System.out.println(file);
        if(!file.exists() || !file.isDirectory() || !file.canRead()) {
            System.err.println("Provide a path to an existing readable directory");
            System.exit(2);
        }

        SourceRoot root = new SourceRoot(file.toPath());
        Tcc tcc = new Tcc();
        root.parse("", (localPath, absolutePath, result) -> {
            result.ifSuccessful(unit -> unit.accept(tcc, null));
            return SourceRoot.Callback.Result.DONT_SAVE;
        });

        // generate Histogram
        generateHistogram(tcc.getTccs(), file.getName());

        // generate graph for each class
       generateGraphs(tcc.getDataForGraph());

    }
}