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
    private String currentPackage;
    private Map<String, Double> tccOfEachClass = new HashMap<>();

    private Map<String, Set<String>> methodAndVariables = new HashMap<>();

    private Map<String, List<PairM>> dataForGraph = new HashMap<>();
    private final FileOutputStream report = new FileOutputStream(new File("report5.txt"));

    public Tcc() throws IOException {
        report.write("Package Name, Class Name, TCC".getBytes(StandardCharsets.UTF_8));
    }

/*    private final FileOutputStream report = new FileOutputStream(new File("tcc.txt"));

    private String currentPackage = "";

    public Tcc() throws IOException {
        report.write("Private field | Class Name | Package".getBytes(StandardCharsets.UTF_8));
        report.write("\n----------------------------------".getBytes());
    }*/

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        // get Package Name
        unit.getPackageDeclaration().ifPresent(pack -> currentPackage = pack.getNameAsString());
        for(ClassOrInterfaceDeclaration cc : unit.findAll(ClassOrInterfaceDeclaration.class)) {
            // ignore test class
            if (cc.getNameAsString().toLowerCase().contains("test"))
                continue;
            cc.accept(this, null);
        }
    }

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
        globalVariableNames.removeAll(localVariables);
        globalVariableNames.removeAll(parameterNames);

        return globalVariableNames;
    }

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
       tccOfEachClass.put(cc.getNameAsString(), calculTCC(methodAndVariables, cc.getNameAsString()));
   }


    public double[] getTccs() {
        return tccOfEachClass.values().stream().mapToDouble(Double::doubleValue).toArray();
    }

    public Map<String, List<PairM>> getDataForGraph() {
        return dataForGraph;
    }
}
