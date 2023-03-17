package exercices.exo5;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
public class Tcc extends VoidVisitorAdapter<Void> {
    private String currentPackage = "Default";
    // Map contains name of class as key and Tcc as value
    private Map<String, Double> tccOfEachClass = new HashMap<>();

    private Map<String, Set<String>> methodAndVariables = new HashMap<>();

    private Map<String, List<PairM>> dataForGraph = new HashMap<>();
    private final FileOutputStream report;

    public Tcc() throws IOException {
        report = new FileOutputStream(new File("Reports/tcc-report.txt"));
        report.write("Package Name, Class Name, TCC".getBytes(StandardCharsets.UTF_8));
        report.write("\n---------------------------".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        // get Package Name
        unit.getPackageDeclaration().ifPresent(pack -> currentPackage = pack.getNameAsString());
        for(ClassOrInterfaceDeclaration cc : unit.findAll(ClassOrInterfaceDeclaration.class)) {
            // ignore test classes
            if (cc.getNameAsString().toLowerCase().contains("test"))
                continue;
            cc.accept(this, null);
        }
    }

    /**
     * Get all global variable used in method
     * @param method : target method
     * @return Set of String (variables name)
     */
    public Set<String> getGlobalVariableNames(MethodDeclaration method) {
        Set<String> localVariables = method.findAll(VariableDeclarator.class)
                .stream()
                .map(VariableDeclarator::getNameAsString)
                .collect(Collectors.toSet());

        Set<String> parameterNames = method.findAll(Parameter.class)
                .stream()
                .map(Parameter::getNameAsString)
                .collect(Collectors.toSet());

        Set<String> globalVariableNames = method.findAll(FieldAccessExpr.class)
                .stream()
                .map(FieldAccessExpr::getNameAsString)
                .collect(Collectors.toSet());

        globalVariableNames.addAll(
                method.findAll(NameExpr.class)
                        .stream()
                        .map(NameExpr::getNameAsString)
                        .collect(Collectors.toSet())
        );
        // remove local variables
        globalVariableNames.removeAll(localVariables);
        // remove parameter variables
        globalVariableNames.removeAll(parameterNames);

        return globalVariableNames;
    }

    /**
     * Method that return the calculated TCC of a class
     * it also prepare data for graph creation method
     * @param methodAndVariables Map of methods and its variables
     * @param className the target class name
     * @return double : tcc value
     */
    public double calculTCC(Map<String, Set<String>> methodAndVariables, String className) {
        int numberMethods = methodAndVariables.size();
        int numPairMethods = (numberMethods * (numberMethods - 1)) / 2;
        List<String> keys = new ArrayList<>(methodAndVariables.keySet());
        int cohesion = 0;
        List<PairM> pairMS = new ArrayList<>();
        for (int i = 0; i<keys.size() - 1; i++) {
            for (int j = i+1; j<keys.size(); j++) {
                Set<String> sharedVariables = new HashSet<>(methodAndVariables.get(keys.get(i)));
                sharedVariables.retainAll(methodAndVariables.get(keys.get(j)));
                if (sharedVariables.size() > 0) {
                    cohesion++;
                    pairMS.add(new PairM(
                            keys.get(i),
                            keys.get(j),
                            String.join(",", sharedVariables)));
                }
            }
        }
        // add this pair method (its shared variables)
        if (pairMS.size() > 0) {
            dataForGraph.put(className, pairMS);
        }
        return numPairMethods == 0 ? 0.0 : (double) cohesion / numPairMethods;
    }

   @Override
    public void visit(ClassOrInterfaceDeclaration cc, Void arg) {
        methodAndVariables = new HashMap<>();
        cc.getMethods().forEach(method -> {
            methodAndVariables.put(method.getNameAsString(), getGlobalVariableNames(method));
        });
        // calculate the TCC of each class
       double tccValue = calculTCC(methodAndVariables, cc.getNameAsString());
       tccOfEachClass.put(cc.getNameAsString(), tccValue);

       // update the file report
       // add to report a new line contain (package, className, Tcc value)
       String newline = String.format("\n%s | %s | %s",
               currentPackage,
               cc.getNameAsString(),
               tccValue
               );
       try {
           report.write(newline.getBytes());
       } catch (IOException e) {
           throw new RuntimeException(e);
       }

   }


    /**
     * return all tcc values of the project
     * used to generate histogram
      * @return double values in array
     */
   public double[] getTccs() {
        return tccOfEachClass.values().stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * return used data to generate dependency graph of each class
     * @return Map contain the class name as key and a list of PairM as value
     */
    public Map<String, List<PairM>> getDataForGraph() {
        return dataForGraph;
    }
}
