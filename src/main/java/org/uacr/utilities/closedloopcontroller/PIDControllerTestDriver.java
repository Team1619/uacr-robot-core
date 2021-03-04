package org.uacr.utilities.closedloopcontroller;

//TEMPORARY CLASS: delete before merging to master branch
public class PIDControllerTestDriver {
    public static void main(String[] args) {
        PIDController controller = new PIDController();

        controller.setSetPoint(0.5);

        //Integral proportional to time (fixed sensorValue)
        for (int i = 1; i <= 20; i++) {
            controller.updateControlValue(0,i * 30);
            System.out.println(controller);
        }
        System.out.println("New Loop");
        for (int i = 1; i <= 40; i++) {
            controller.updateControlValue(i * 0.05,600 + (i * 30));
            System.out.println(controller);
        }
    }
}
