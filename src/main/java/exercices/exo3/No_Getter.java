package exercices.exo3;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorWithDefaults;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * This class produce a txt report contains private fields of public classes that have no public getter
 * the report contains : name of field, their class Name, and the package of class
 */
public class No_Getter extends VoidVisitorWithDefaults<Void> {
    private final FileOutputStream report;

    private String currentPackage = "Default";

    /**
     * prepare the file of our result
     * @throws IOException
     */
    public No_Getter() throws IOException {
        report = new FileOutputStream(new File("Reports/No-getter-Report.txt"));
        report.write("Private field | Class Name | Package".getBytes(StandardCharsets.UTF_8));
        report.write("\n----------------------------------".getBytes());
    }

    @Override
    public void visit(CompilationUnit unit, Void arg) {
        // get Package Name
        unit.getPackageDeclaration().ifPresent(pack -> currentPackage = pack.getNameAsString());

        for(ClassOrInterfaceDeclaration type : unit.findAll(ClassOrInterfaceDeclaration.class)) {
            type.accept(this, null);
        }
    }

    @Override
    public void visit(ClassOrInterfaceDeclaration declaration, Void arg) {
        // iterate over private fields
        for (FieldDeclaration field: declaration.getFields()) {
            if (field.isPrivate()) {
                field.getVariables().forEach(v -> {
                    String nameField = v.getNameAsString();
                    String typeField = v.getTypeAsString();
                    String nameGetterMd = "";
                    // default nomination method for boolean fields
                    if (typeField.equals("boolean")) {
                        nameGetterMd = "is" + nameField.substring(0,1).toUpperCase() +
                                nameField.substring(1);
                    } else {
                        // default nomination method for other types
                        nameGetterMd = "get" + nameField.substring(0,1).toUpperCase() +
                                nameField.substring(1);
                    }

                    List<MethodDeclaration> getter = declaration.getMethodsByName(nameGetterMd);
                    if (getter == null ||
                            getter.size() == 0 ||
                            getter.get(0).isPrivate() ||
                            // check the return type, it should be the same as the field
                            !getter.get(0).getTypeAsString().equals(v.getTypeAsString())
                    ) {
                        // found issue => write to file
                        // new line
                        String line = String.format("\n%s | %s | %s", nameField, declaration.getNameAsString(), currentPackage);
                        try {
                            report.write(line.getBytes());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }

    }
}
