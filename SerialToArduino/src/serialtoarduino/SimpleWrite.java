/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serialtoarduino;

/**
 *
 * @author pbibu
 */
import arduino.*;
import static java.lang.Thread.sleep;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner; 



public class SimpleWrite {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        Arduino obj = new Arduino("COM3", 9600);
        obj.openConnection();
        try {
            sleep(8000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleWrite.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(obj.getPortDescription());
        System.out.println(obj.serialRead());
        char lastChar = ' ';
        System.out.println("Please type in command to use: ");
        while(lastChar != 'x'){
            if(scan.hasNext()){
                char hold = scan.next().charAt(0);
                if(hold != lastChar){
                    lastChar = hold;
                    System.out.println("Changing to " + lastChar);
                    System.out.println("Please type in command to use: ");
                }
                obj.serialWrite(lastChar);
            }
        }
        obj.closeConnection();
    }
}
