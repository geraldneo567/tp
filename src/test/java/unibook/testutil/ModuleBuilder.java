package unibook.testutil;

import java.util.HashSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import unibook.model.module.Module;
import unibook.model.module.ModuleCode;
import unibook.model.module.ModuleName;
import unibook.model.person.Email;
import unibook.model.person.Name;
import unibook.model.person.Office;
import unibook.model.person.Phone;
import unibook.model.person.Professor;
import unibook.model.person.Student;

/**
 * A utility class to help with building Module objects.
 */
public class ModuleBuilder {

    public static final String DEFAULT_CODE = "CS2103";
    public static final String DEFAULT_NAME = "Software Engineering";
    public static final String DEFAULT_PROFESSOR = "Damith";
    public static final Phone DEFAULT_PHONE_NUMBER = new Phone("12345678");
    public static final Email DEFAULT_EMAIL = new Email("damith@nus.edu.sg");
    public static final Office DEFAULT_OFFICE = new Office("COM1-1");

    private ModuleName moduleName;
    private ModuleCode moduleCode;
    private ObservableList<Professor> professors;
    private ObservableList<Student> students;

    /**
     * Creates a {@code ModuleBuilder} with the default details.
     */
    public ModuleBuilder() {
        moduleName = new ModuleName(DEFAULT_NAME);
        moduleCode = new ModuleCode(DEFAULT_CODE);
        professors = FXCollections.observableArrayList();
        professors.add(new Professor(new Name(DEFAULT_PROFESSOR),
            new Phone("98765432"), new Email("test@nus.edu.sg"), new HashSet<>(), new Office("SOC"), new HashSet<>()));
        students = FXCollections.observableArrayList();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public ModuleBuilder(Module moduleToCopy) {
        moduleName = moduleToCopy.getModuleName();
        moduleCode = moduleToCopy.getModuleCode();
        professors = moduleToCopy.getProfessors();
        students = moduleToCopy.getStudents();
    }

    /**
     * Sets the {@code moduleName} of the {@code Module} that we are building.
     */
    public ModuleBuilder withModuleName(String name) {
        this.moduleName = new ModuleName(name);
        return this;
    }

    /**
     * Sets the {@code moduleCode} of the {@code Module} that we are building.
     */
    public ModuleBuilder withModuleCode(String code) {
        this.moduleCode = new ModuleCode(code);
        return this;
    }

    /**
     * Sets the {@code professor} of the {@code Module} that we are building.
     */
    public ModuleBuilder withProfessor(String profName) {
        this.professors = FXCollections.observableArrayList();
        professors.add(new Professor(new Name(profName), DEFAULT_PHONE_NUMBER,
                DEFAULT_EMAIL, new HashSet<>(), DEFAULT_OFFICE, new HashSet<>()));
        return this;
    }

    public Module build() {
        return new Module(moduleName, moduleCode, professors, students);
    }

}