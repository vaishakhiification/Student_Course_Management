public class Student {
    int StudentId;
    String StudentFirstName;
    String StudentLastName;

    Student(){
        StudentId = 0;
        StudentFirstName = "";
        StudentLastName = "";
    }

    Student(int studentId, String studentFirstName, String studentLastName){
        StudentId = studentId;
        StudentLastName = studentLastName;
        StudentFirstName = studentFirstName;
    }

}
