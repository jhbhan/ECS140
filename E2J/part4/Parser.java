/* *** This file is given as part of the programming assignment. *** */
import java.util.Hashtable;
import java.util.Stack;

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	tok = scanner.scan();
    }
    Token temp;
    Token temp2;
    int assignmentChecker = 0;
    //if 0, then not assginment, if 1, assginment
    int ifwhilecheck = 0;
    //0 if didn't have tilda, 1 if did have tilda

    //if 0, if, if 1, then while

    SymbolsTable symTable = new SymbolsTable();
    private Scan scanner;

    Parser(Scan scanner) {
	this.scanner = scanner;
	scan();
	program();
	if( tok.kind != TK.EOF )
	    parse_error("junk after logical end of program");
    }

    private void program() {
		System.out.println("public class My_e2j{");
		System.out.println("public static void main(String[] args){");
		block();
		System.out.println("}");
		System.out.println("}");

    }

    private void block(){
	declaration_list();
	statement_list();
    }

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
	while( is(TK.DECLARE) ) {
	    declaration();
	}
    }

    private void declaration() {
		mustbe(TK.DECLARE);
		temp = tok;
		mustbe(TK.ID);
	if(!(symTable.containsSymbol(temp))) {
		System.out.println("int x_" + temp.string + "_" + symTable.getScopeMarker() + ";");
	}
	symTable.insertSymbol(temp);
	while(is(TK.COMMA)){
	    scan();
	    temp = tok;
	    mustbe(TK.ID);
		if(!(symTable.containsSymbol(temp))) {
			System.out.println("int x_" + temp.string + "_" + symTable.getScopeMarker()+ ";");
		}
	    symTable.insertSymbol(temp);
	}
    }

    private void statement_list() {
    	while(isStatement()){
    		statement();
    	}
    } 

    private boolean isStatement(){
    	if((isAssignment() || is(TK.PRINT)) || (is(TK.DO) || is(TK.IF))){
    		return true;
    	}
    	else {return false;}
    }

	private boolean isAssignment(){
		if(is(TK.TILDE) || is(TK.ID)){
			return true;
		}
		return false;
	}

    private void statement(){
    	if(isAssignment()){
    		tkAssign();
    	}
    	else if(is(TK.PRINT)){
    		tkPrint();
    	}else if(is(TK.DO)){
    		tkDo();
    	}else if(is(TK.IF)){
    		tkIf();
 	    }else{
	    	parse_error("asdf1");
	    }
    }

    private void tkAssign(){
    	ref_id();
    	mustbe(TK.ASSIGN);
		System.out.println("=");
		assignmentChecker = 1;
    	expression();
    	assignmentChecker = 0;
    }

    private void tkPrint(){
    	mustbe(TK.PRINT);
		System.out.println("System.out.println(");
    	expression();
		System.out.println(");");
    }

    private void tkDo(){
    	ifwhilecheck = 1;
    	mustbe(TK.DO);
    	System.out.println("while(");
    	guarded_command();
    	mustbe(TK.ENDDO);
    }

    private void tkIf(){
    	ifwhilecheck = 0;
    	mustbe(TK.IF);
    	System.out.println("if(");
    	guarded_command();
    	while(is(TK.ELSEIF)){
    		System.out.println("else if(");
    		scan();
    		guarded_command();
    	}
    	if(is(TK.ELSE)){
    		System.out.println("else{");
    		scan();
            symTable.newScope();
    		block();
            symTable.endScope();
            System.out.println("}");
    	}
    	mustbe(TK.ENDIF);
    }

    private void expression(){
    	term();
    	while(is(TK.PLUS) || is(TK.MINUS)){
    		if (is(TK.PLUS)) {
    			System.out.println("+");
			}else{
				System.out.println("-");
			}
    		scan();
    		term();
    	}if(assignmentChecker == 1){System.out.println(";");}

    }

    private void term(){
    	factor();
    	while(is(TK.TIMES) || is (TK.DIVIDE)){
    		if (is(TK.TIMES)){
				System.out.println("*");
			}else{
				System.out.println("/");
			}
    		scan();
    		factor();
    	}
    }

    private void factor(){
    	if(is(TK.LPAREN)){
			System.out.println("(");
    		scan();
    		expression();
    		mustbe(TK.RPAREN);
			System.out.println(")");
    	}else if(isRef_id()){
    		ref_id();
    	}else{
    		temp = tok;
    		mustbe(TK.NUM);
    		System.out.println(temp.string);

//    		if(assignmentChecker == 0){
//    		System.out.println(temp.string);}
//    		else{
//    			System.out.println(temp.string);
//    		}
    	}
    }

    private boolean isRef_id(){
    	if(is(TK.TILDE) || is(TK.ID)){
    		return true;
    	}
    	else{return false;}
    }

    private void ref_id(){ 
    int scope = -2;//-2 is special case for no scope
    if(is(TK.TILDE)){
            scope = -1;
    		scan();
    		if(is(TK.NUM)){
                scope = Integer.parseInt(tok.string);
    			scan();
    		}
    	}
        temp = tok;
    	mustbe(TK.ID);
    	if(scope == -2) {
			System.out.println("x_" + temp.string + "_" + symTable.locateScope(temp));
		}else if(scope == -1){
			System.out.println("x_" + temp.string + "_0");
		}else if(scope == 0){
			System.out.println("x_" + temp.string + "_" + Integer.toString(symTable.getScopeMarker()));
		}
		else{
			System.out.println("x_" + temp.string + "_" + Integer.toString(symTable.getScopeMarker()-scope));
		}



        if((symTable.getScopeMarker() - scope) < 0){
                    System.err.println("no such variable ~"  + scope + temp.string + " on line " + temp.lineNumber);
                    System.exit(1);
                }
        if (scope == -1){//global check
            symTable.locateVariableGlobal(temp);
        }else if (scope > -1){//scope number
            symTable.locateVariable(temp, scope);
        }else if (scope == -2){//closest nested
            symTable.locateVariable(temp);
        }else{
           symTable.assignValue(temp);
        }
    }

    private void guarded_command(){
        expression();
    	mustbe(TK.THEN);
    	//if(ifwhilecheck == 0){
    	System.out.println("<= 0){");
    	//}else{
    //		System.out.println("<= 0){");
    //	}
        symTable.newScope();
    	block();
        symTable.endScope();
        System.out.println("}");
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
