package org.example;

import java.io.File;
import java.util.*;

import com.clarkparsia.owlapi.explanation.DefaultExplanationGenerator;
import com.clarkparsia.owlapi.explanation.util.SilentExplanationProgressMonitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import static org.semanticweb.HermiT.Reasoner.ReasonerFactory;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static OWLOntology ontology;
    private static OWLReasoner reasoner;
    private static DefaultExplanationGenerator explanationGenerator;
    private static ConvertToNaturalLanguage convertor;
    private static Set<OWLClass> classes;

    public static void main(String[] args) {
        initialize();
        chooseSubclass();
    }

    private static void initialize() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            ontology = manager.loadOntologyFromOntologyDocument(new File("pizza.owl"));
            OWLReasonerFactory rf = new ReasonerFactory();
            reasoner = rf.createReasoner(ontology);
            SilentExplanationProgressMonitor progressMonitor = new SilentExplanationProgressMonitor();
            explanationGenerator = new DefaultExplanationGenerator(manager, rf, ontology, progressMonitor);
            convertor = new ConvertToNaturalLanguage();
            classes = ontology.getClassesInSignature();
        } catch (Exception e) {
            System.out.println("An Unexpected error occurred!");
        }
    }

    private static void chooseSubclass() {
        List<OWLClass> sortedClasses = displayClasses();
        String input = scanner.next();
        if (input.equalsIgnoreCase("q")) {
            System.exit(0);
        } else {
            int subClassIndex = Integer.parseInt(input);
            OWLClass subClass = (OWLClass) sortedClasses.toArray()[subClassIndex - 1];
            System.out.println("\nChosen subclass is: " + subClass.getIRI().getShortForm());
            chooseSuperclass(subClass);
        }
    }

    private static void chooseSuperclass(OWLClass subClass) {
        NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(subClass, true);
        List<OWLClass> sortedSuperClasses = displaySuperclasses(superClasses, subClass);
        System.out.println("\nChoose a superclass, or enter 'b' to go back, or 'q' to quit: ");
        String input = scanner.next();
        if (input.equalsIgnoreCase("b")) {
            chooseSubclass();
        } else if (input.equalsIgnoreCase("q")) {
            System.exit(0);
        } else {
            int superClassIndex = Integer.parseInt(input);
            OWLClass superClass = (OWLClass) sortedSuperClasses.toArray()[superClassIndex - 1];
            System.out.println("\nChosen superclass is: " + superClass.getIRI().getShortForm());
            chooseExplanation(superClass, subClass);
        }
    }

    private static List<OWLClass> displayClasses() {
        System.out.println("\nClasses in the ontology (in alphabetical order):");
        List<OWLClass> sortedClasses = new ArrayList<>(classes);
        sortedClasses.sort(Comparator.comparing(owlClass -> owlClass.getIRI().getShortForm()));
        int index = 1;
        for (OWLClass owlClass : sortedClasses) {
            System.out.println(index + ". " + owlClass.getIRI().getShortForm());
            index++;
        }
        System.out.println("\nChoose a subclass, or enter 'q' to quit: ");
        return sortedClasses;
    }

    private static List<OWLClass> displaySuperclasses(NodeSet<OWLClass> superclasses, OWLClass subClass) {
        System.out.println("Superclasses of " + subClass.getIRI().getShortForm() + " (in alphabetical order):");
        List<OWLClass> sortedSuperclasses = new ArrayList<>(superclasses.getFlattened());
        sortedSuperclasses.sort(Comparator.comparing(owlClass -> owlClass.getIRI().getShortForm()));
        int index = 1;
        for (OWLClass owlClass : sortedSuperclasses) {
            System.out.println(index + ". " + owlClass.getIRI().getShortForm());
            index++;
        }
        return sortedSuperclasses;
    }

    private static void chooseExplanation(OWLClass superClass, OWLClass subClass) {
        try {
            OWLSubClassOfAxiom subclassAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(subClass, superClass);

            System.out.println("\nChoose the type of explanation:");
            System.out.println("1. Minimal set of explanations");
            System.out.println("2. Full Set of explanations");
            System.out.println("Or press 'b' to go back");
            String choice = scanner.next();

            if(choice.equalsIgnoreCase("b")) {
                chooseSuperclass(subClass);
            } else if (Integer.parseInt(choice) == 1) {
                System.out.println("Chosen explanation: 1. Minimal set of explanations");
                displayExplanation(subclassAxiom);
            } else if (Integer.parseInt(choice) == 2) {
                System.out.println("Chosen explanation: 2. Full Set of explanations");
                displayExplanations(subclassAxiom);
            } else {
                System.out.println("Invalid choice. Please choose 1 or 2, or press 'b' to go back.");
            }

            System.out.println("Press 'b' to go back, or 'r' to start again, or 'q' to quit");
            String input = scanner.next();
            if (input.equalsIgnoreCase("b")) {
                chooseExplanation(superClass, subClass);
            } else if (input.equalsIgnoreCase("r")) {
                chooseSubclass();
            }
            else if (input.equalsIgnoreCase("q")) {
                System.exit(0);
            }
        } catch (Exception e) {
            System.out.println("An Unexpected error occurred!");
        }
    }

    private static void displayExplanation(OWLSubClassOfAxiom subclassAxiom) {
        Set<OWLAxiom> explanation = explanationGenerator.getExplanation(subclassAxiom);
        System.out.println("\nWhy can we say that " + subclassAxiom.accept(convertor) + "? \nBecause:");
        for (OWLAxiom axiom : explanation) {
            String explanationString = axiom.accept(convertor);
            System.out.println(explanationString);
        }
    }

    private static void displayExplanations(OWLSubClassOfAxiom subclassAxiom) {
        System.out.println("\nWhy can we say that " + subclassAxiom.accept(convertor) + "? \nBecause:");
        Set<Set<OWLAxiom>> explanations = explanationGenerator.getExplanations(subclassAxiom);
        Set<OWLAxiom> visitedAxioms = new HashSet<>();
        int index = 1;
        for (Set<OWLAxiom> explanation : explanations) {
            for (OWLAxiom axiom : explanation) {
                if(!visitedAxioms.contains(axiom)) {
                    String explanationString = axiom.accept(convertor);
                    System.out.println(index + ". " + explanationString);
                    visitedAxioms.add(axiom);
                    index++;
                }
            }
        }
    }
}

