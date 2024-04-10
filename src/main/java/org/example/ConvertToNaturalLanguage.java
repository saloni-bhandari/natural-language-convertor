package org.example;

import org.semanticweb.owlapi.model.*;

import java.util.Set;

/*
    Convert OWL to natural language
 */

public class ConvertToNaturalLanguage
        implements OWLClassExpressionVisitorEx<String>,
        OWLAxiomVisitorEx<String>,
        OWLObjectVisitorEx<String> {

    public String visit(OWLClass ce) {
        return ce.getIRI().getShortForm();
    }

    public String visit(OWLObjectComplementOf ce) {
        return " does not " + ce.getOperand().accept(this);
    }

    public String visit(OWLObjectAllValuesFrom ce) {
        return "has " + ce.getProperty().asOWLObjectProperty().getIRI().getShortForm() + " that is " + ce.getFiller().accept(this);
    }

    public String visit(OWLObjectIntersectionOf ce) {
        String result = "";
        boolean first = true;
        for (OWLClassExpression conjunct : ce.getOperands()) {
            if (!first) {
                result += " and ";
            }
            result += conjunct.accept(this);
            first = false;
        }
        return result;
    }

    public String visit(OWLObjectHasValue ce) {
        String property = ce.getProperty().asOWLObjectProperty().getIRI().getShortForm();
        String value = ce.getValue().asOWLNamedIndividual().getIRI().getShortForm();
        return "has " + property + " with value " + value;
    }

    public String visit(OWLObjectOneOf ce) {
        Set<OWLIndividual> individuals = ce.getIndividuals();
        String result = "is one of: ";
        boolean first = true;
        for (OWLIndividual individual : individuals) {
            if (!first) {
                result += ", ";
            }
            result += individual.asOWLNamedIndividual().getIRI().getShortForm();
            first = false;
        }
        return result;
    }

    public String visit(OWLObjectSomeValuesFrom ce) {
        return "has some " + ce.getProperty().asOWLObjectProperty().getIRI().getShortForm() + " that is " + ce.getFiller().accept(this);
    }

    public String visit(OWLObjectUnionOf ce) {
        String result = "either ";
        boolean first = true;
        for (OWLClassExpression disjunct : ce.getOperands()) {
            if (!first) {
                result += " or ";
            }
            result += disjunct.accept(this);
            first = false;
        }
        return result;
    }

    public String visit(OWLObjectMinCardinality ce) {
        int cardinality = ce.getCardinality();
        String property = ce.getProperty().asOWLObjectProperty().getIRI().getShortForm();
        String filler = ce.getFiller().accept(this);
        return "has at least " + cardinality + " " + property + " that are " + filler;
    }

    public String visit(OWLEquivalentClassesAxiom axiom) {
        String result = "";
        boolean first = true;
        for (OWLClassExpression ce : axiom.getClassExpressions()) {
            if (!first) {
                result += " is defined as ";
            }
            result += ce.accept(this);
            first = false;
        }
        return result;
    }

    public String visit(OWLDisjointClassesAxiom axiom) {
        String result = "";
        boolean first = true;
        for (OWLClassExpression ce : axiom.getClassExpressions()) {
            if (!first) {
                result += " and ";
            }
            result += ce.accept(this);
            first = false;
        }
        return result + " do not overlap.";
    }

    public String visit(OWLObjectPropertyDomainAxiom axiom) {
        String property = axiom.getProperty().asOWLObjectProperty().getIRI().getShortForm();
        String domain = axiom.getDomain().accept(this);
        return property + " is in the domain " + domain;
    }

    public String visit(OWLObjectPropertyRangeAxiom axiom) {
        String property = axiom.getProperty().asOWLObjectProperty().getIRI().getShortForm();
        String range = axiom.getRange().accept(this);
        return property + " has the range " + range;
    }

    public String visit(OWLSubClassOfAxiom axiom) {
        String superClass = axiom.getSuperClass().accept(this);
        String subClass = axiom.getSubClass().accept(this);
        if (axiom.getSuperClass() instanceof OWLClass)
            return "Every " + subClass + " is a " + superClass;
        else
            return "Every " + subClass + " " + superClass;
    }

    public String visit(OWLFunctionalObjectPropertyAxiom axiom) {
        String property = axiom.getProperty().asOWLObjectProperty().getIRI().getShortForm();
        return property + " can only have one value";
    }

    public String visit(OWLSubObjectPropertyOfAxiom axiom) {
        String subProperty = axiom.getSubProperty().asOWLObjectProperty().getIRI().getShortForm();
        String superProperty = axiom.getSuperProperty().asOWLObjectProperty().getIRI().getShortForm();
        return "Every " + subProperty + " is a " + superProperty;
    }

    public String visit(OWLInverseObjectPropertiesAxiom axiom) {
        String property1 = axiom.getFirstProperty().asOWLObjectProperty().getIRI().getShortForm();
        String property2 = axiom.getSecondProperty().asOWLObjectProperty().getIRI().getShortForm();
        return property1 + " is the inverse of " + property2;
    }


}
