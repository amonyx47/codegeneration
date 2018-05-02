package sk.fmph.uniba.dp.errors;

public class ElementNotFoundException extends Exception{

    public ElementNotFoundException(String nameOfElement){
        super(nameOfElement);
    }

}
