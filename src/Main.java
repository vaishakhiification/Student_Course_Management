import com.mysql.cj.protocol.Resultset;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASS = "root";
    private static Connection connection = null;
    private static Statement statement = null;

    //connection methods
    private static void initialiseDBConnection() {
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
        } catch (SQLException ex) {
            System.out.println(ex.getNextException());
        } catch (ClassNotFoundException ex) {
            System.out.println(ex.getException());
        }
    }

    private static void closeDBConnection() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    //initialisation methods
    private static void initialiseDB() {
        try {
            String query = "CREATE DATABASE IF NOT EXISTS Student_Course_Management";
            int result = statement.executeUpdate(query);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void createTables() {
        try {
            String query_use_db = "USE Student_Course_Management";
            String query_create_student = "CREATE TABLE IF NOT EXISTS Student (\n" +
                    "    StudentId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    StudentFirstName VARCHAR(200) NOT NULL DEFAULT '',\n" +
                    "    StudentLastName VARCHAR(200) NOT NULL DEFAULT ''\n" +
                    ");";
            String query_create_course = "CREATE TABLE IF NOT EXISTS Course (\n" +
                    "    CourseId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    CourseName VARCHAR(200) NOT NULL DEFAULT ''\n" +
                    ");";
            String query_create_schedule = "CREATE TABLE IF NOT EXISTS Course_Schedule (\n" +
                    "    ScheduleId INT NOT NULL AUTO_INCREMENT PRIMARY KEY,\n" +
                    "    CourseId INT NOT NULL,\n" +
                    "    StartTime TIME(0) NOT NULL,\n" +
                    "    EndTime TIME(0) NOT NULL,\n" +
                    "    DayInTheWeek ENUM('Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'),\n" +
                    "    FOREIGN KEY (CourseId)\n" +
                    "        REFERENCES Course (CourseId)\n" +
                    ");";
            String query_create_student_schedule = "CREATE TABLE IF NOT EXISTS Student_Schedule (\n" +
                    "    StudentId INT NOT NULL,\n" +
                    "    ScheduleId INT NOT NULL,\n" +
                    "    PRIMARY KEY (StudentId , ScheduleId),\n" +
                    "    FOREIGN KEY (ScheduleId)\n" +
                    "        REFERENCES Course_Schedule (ScheduleId),\n" +
                    "    FOREIGN KEY (StudentId)\n" +
                    "        REFERENCES Student (StudentId)\n" +
                    ");";
            statement.executeUpdate(query_use_db);
            statement.executeUpdate(query_create_student);
            statement.executeUpdate(query_create_course);
            statement.executeUpdate(query_create_schedule);
            statement.executeUpdate(query_create_student_schedule);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    //add new records
    private static int addNewStudent(Student request) throws SQLException {
        int response = 0;
        String query = String.format("INSERT INTO Student (StudentId, StudentFirstName, StudentLastName) values " +
                "(default, '%s', '%s' );", request.StudentFirstName, request.StudentLastName);
        response = statement.executeUpdate(query);
        return response;
    }

    private static int addNewCourse(Course request) throws SQLException {
        int response = 0;
        String query = String.format("INSERT INTO Course (CourseId, CourseName) values " +
                "(default, '%s');", request.CourseName);
        response = statement.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = statement.getGeneratedKeys();
        if (rs.next()) {
            response = rs.getInt(1);
        }
        return response;
    }

    private static int addNewCourseSchedule(CourseSchedule request) throws SQLException {
        int response = 0;
        String query = String.format("INSERT INTO Course_Schedule (ScheduleId, CourseId, StartTime, EndTime, DayInTheWeek) \n" +
                "values (default, %d, '%s', '%s', %d);", request.CourseId, request.StartTime.toString(), request.EndTime.toString(), request.DayInTheWeek);
        response = statement.executeUpdate(query);
        return response;
    }

    private static int addNewStudentSchedule(StudentSchedule request) throws SQLException {
        int response = 0;
        String query = String.format("INSERT INTO Student_Schedule (StudentId, ScheduleId) values " +
                "(%d, %d);", request.StudentId, request.ScheduleId);
        response = statement.executeUpdate(query);
        return response;
    }

    //get methods
    private static List<Course> getCourses(Course courseRequest, Student studentRequest) throws SQLException {
        List<Course> response = new ArrayList();
        String query = "";
        if (courseRequest != null) {
            query = String.format("SELECT DISTINCT\n" +
                    "    CourseId, CourseName\n" +
                    "FROM\n" +
                    "    Course\n" +
                    "WHERE\n" +
                    "    CourseId = %d" +
                    "ORDER BY CourseId;", courseRequest.CourseId);
        } else if (studentRequest != null) {
            query = String.format("SELECT DISTINCT\n" +
                    "    c.CourseId, c.CourseName\n" +
                    "FROM\n" +
                    "    Course c\n" +
                    "        INNER JOIN\n" +
                    "    Course_Schedule cs ON c.CourseId = cs.CourseId\n" +
                    "        INNER JOIN\n" +
                    "    Student_Schedule ss ON ss.ScheduleId = cs.ScheduleId\n" +
                    "        INNER JOIN\n" +
                    "    Student s ON s.StudentId = ss.StudentId\n" +
                    "WHERE\n" +
                    "    s.StudentId = %d\n" +
                    "ORDER BY CourseId;", studentRequest.StudentId);
        } else {
            query = "SELECT DISTINCT\n" +
                    "    CourseId, CourseName\n" +
                    "FROM\n" +
                    "    Course";
        }
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            Course course = new Course(result.getInt("CourseId")
                    , result.getString("CourseName"));
            response.add(course);
        }
        return response;
    }

    private static List<Student> getStudents(Course course) throws SQLException {
        List<Student> response = new ArrayList();
        String query = "";
        if (course != null) {
            query = String.format("SELECT DISTINCT\n" +
                    "    s.StudentId, s.StudentFirstName, s.StudentLastName\n" +
                    "FROM\n" +
                    "    Student s\n" +
                    "        INNER JOIN\n" +
                    "    Student_Schedule ss ON s.StudentId = ss.StudentId\n" +
                    "        INNER JOIN\n" +
                    "    Course_Schedule cs ON cs.ScheduleId = ss.ScheduleId\n" +
                    "        INNER JOIN\n" +
                    "    Course c ON c.CourseId = cs.CourseId\n" +
                    "WHERE\n" +
                    "    c.CourseId = %d\n" +
                    "ORDER BY s.StudentId;", course.CourseId);
        } else {
            query = "SELECT DISTINCT\n" +
                    "    s.StudentId, s.StudentFirstName, s.StudentLastName\n" +
                    "FROM\n" +
                    "    Student s\n" +
                    "ORDER BY s.StudentId;";
        }
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            Student student = new Student(result.getInt("StudentId")
                    , result.getString("StudentFirstName")
                    , result.getString("StudentLastName"));
            response.add(student);
        }
        return response;
    }

    private static List<CourseSchedule> getCourseSchedule(Student studentRequest, CourseSchedule scheduleRequest) throws SQLException {
        List<CourseSchedule> response = new ArrayList();
        String query = "";
        if (studentRequest != null && scheduleRequest != null) {
            query = String.format("SELECT DISTINCT\n" +
                    "    cs.ScheduleId,\n" +
                    "    c.CourseId,\n" +
                    "    c.CourseName,\n" +
                    "    cs.StartTime,\n" +
                    "    cs.EndTime,\n" +
                    "    CONVERT(cs.Dayintheweek, SIGNED) AS Dayintheweek\n" +
                    "FROM\n" +
                    "    Course c\n" +
                    "        INNER JOIN\n" +
                    "    Course_Schedule cs ON c.CourseId = cs.CourseId\n" +
                    "        INNER JOIN\n" +
                    "    Student_Schedule ss ON ss.ScheduleId = cs.ScheduleId\n" +
                    "WHERE\n" +
                    "    ss.StudentId = %d\n" +
                    "        AND cs.DayInTheWeek = %d\n" +
                    "ORDER BY c.CourseName; ", studentRequest.StudentId, scheduleRequest.DayInTheWeek);
        } else {
            query = "SELECT DISTINCT\n" +
                    "    cs.ScheduleId,\n" +
                    "    c.CourseId,\n" +
                    "    c.CourseName,\n" +
                    "    cs.StartTime,\n" +
                    "    cs.EndTime,\n" +
                    "    CONVERT(cs.Dayintheweek, SIGNED) AS Dayintheweek\n" +
                    "FROM\n" +
                    "    Course c\n" +
                    "        INNER JOIN\n" +
                    "    Course_Schedule cs ON c.CourseId = cs.CourseId\n" +
                    "ORDER BY c.CourseName;";
        }
        ResultSet result = statement.executeQuery(query);
        while (result.next()) {
            CourseSchedule courseSchedule = new CourseSchedule(result.getInt("CourseId")
                    , result.getInt("ScheduleId")
                    , result.getString("CourseName")
                    , result.getTime("StartTime")
                    , result.getTime("EndTime")
                    , result.getInt("DayInTheWeek")
            );
            response.add(courseSchedule);
        }
        return response;
    }

    //main
    public static void main(String args[]) {
        try {
            Scanner input = new Scanner(System.in);
            int ch = 0;
            initialiseDBConnection();
            initialiseDB();
            createTables();

            do {

                System.out.println("\n1. Add New Student");
                System.out.println("2. Add New Course");
                System.out.println("3. Enroll Student in Courses");
                System.out.println("4. Check which Students are in which Courses");
                System.out.println("5. Check which Courses each student is in");
                System.out.println("6. Check which Courses and what times each course is for a given student on a given day of the week.");
                System.out.println("7. EXIT!!");
                System.out.println("Please choose: ");

                ch = input.nextInt();
                switch (ch) {
                    case 1: {
                        Student student = new Student();
                        System.out.println("Enter first name: ");
                        student.StudentFirstName = input.next();
                        System.out.println("Enter last name: ");
                        student.StudentLastName = input.next();
                        addNewStudent(student);
                        break;
                    }
                    case 2: {
                        Course course = new Course();
                        CourseSchedule courseSchedule = new CourseSchedule();
                        System.out.println("Enter the Course Name: ");
                        course.CourseName = input.next();
                        courseSchedule.CourseId = addNewCourse(course);
                        System.out.println("Enter Start Time: (in format HH:MM:SS)");
                        courseSchedule.StartTime = Time.valueOf(input.next());
                        System.out.println("Enter End Time: (in format HH:MM:SS)");
                        courseSchedule.EndTime = Time.valueOf(input.next());
                        System.out.println("Enter Day in the week: \n(1.Mon, 2.Tue, 3.Wed, 4.Thu, 5.Fri, 6.Sat, 7.Sun)");
                        courseSchedule.DayInTheWeek = input.nextInt();
                        addNewCourseSchedule(courseSchedule);
                        break;
                    }
                    case 3: {
                        int n = 0;
                        List<CourseSchedule> courses = getCourseSchedule(null, null);
                        List<Student> students = getStudents(null);
                        System.out.println("Student List: ");
                        System.out.println("StudentID   FirstName   LastName");
                        for (Student obj : students) {
                            System.out.println(obj.StudentId + "             " + obj.StudentFirstName + "      " + obj.StudentLastName);
                        }
                        System.out.println("Course Schedule List: ");
                        System.out.println("ScheduleId  CourseName  StartTime   EndTime     DayInTheWeek");
                        for (CourseSchedule obj : courses) {
                            System.out.println(obj.CourseScheduleId + "        "
                                    + obj.CourseName + "   "
                                    + obj.StartTime + "   "
                                    + obj.EndTime + "    "
                                    + Enums.DayInTheWeek.values()[obj.DayInTheWeek -1]);
                        }
                        StudentSchedule studentSchedule = new StudentSchedule();
                        System.out.println("Enter StudentId: ");
                        studentSchedule.StudentId = input.nextInt();
                        System.out.println("Enter Schedule Id: ");
                        studentSchedule.ScheduleId = input.nextInt();
                        addNewStudentSchedule(studentSchedule);
                        break;
                    }
                    case 4: {
                        List<Course> courses = getCourses(null, null);
                        Course course = new Course();
                        System.out.println("Course List: ");
                        System.out.println("CourseId  CourseName");
                        for (Course obj : courses) {
                            System.out.println(obj.CourseId + "  "
                                    + obj.CourseName + " ");
                        }
                        System.out.println("Enter courseId: ");
                        course.CourseId = input.nextInt();
                        List<Student> students = getStudents(course);
                        System.out.println("StudentID   FirstName   LastName");
                        for (Student obj : students) {
                            System.out.println(obj.StudentId + "  " + obj.StudentFirstName + " " + obj.StudentLastName);
                        }
                        break;
                    }
                    case 5: {
                        List<Student> students = getStudents(null);
                        System.out.println("StudentID   FirstName   LastName");
                        for (Student obj : students) {
                            System.out.println(obj.StudentId + "  " + obj.StudentFirstName + " " + obj.StudentLastName);
                        }
                        Student student = new Student();
                        System.out.println("Enter studentId: ");
                        student.StudentId = input.nextInt();
                        List<Course> courses = getCourses(null, student);
                        System.out.println("CourseId  CourseName");
                        for (Course obj : courses) {
                            System.out.println(obj.CourseId + "  "
                                    + obj.CourseName + " ");
                        }
                    }
                    break;
                    case 6: {
                        List<Student> students = getStudents(null);
                        System.out.println("StudentID   FirstName   LastName");
                        for (Student obj : students) {
                            System.out.println(obj.StudentId + "  " + obj.StudentFirstName + " " + obj.StudentLastName);
                        }
                        Student student = new Student();
                        CourseSchedule courseSchedule = new CourseSchedule();
                        System.out.println("Enter studentId: ");
                        student.StudentId = input.nextInt();
                        System.out.println("Enter day: ");
                        System.out.println("1 ->Mon 2->Tue 3->Wed 4->Thu 5->Fri 6->Sat 7->Sun");
                        courseSchedule.DayInTheWeek = input.nextInt();
                        List<CourseSchedule> courses = getCourseSchedule(student, courseSchedule);
                        System.out.println("Course Schedule List: ");
                        System.out.println("ScheduleId  CourseName  StartTime   EndTime     DayInTheWeek");
                        for (CourseSchedule obj : courses) {
                            System.out.println(obj.CourseId + "         "
                                    + obj.CourseName + "    "
                                    + obj.StartTime + "    "
                                    + obj.EndTime + "    "
                                    + Enums.DayInTheWeek.values()[obj.DayInTheWeek -1]);
                        }

                    }
                    default:
                        break;
                }
            } while (ch != 7);
            closeDBConnection();
        } catch (SQLDataException sqlDataEx) {
            System.out.println(sqlDataEx);
            System.out.println(sqlDataEx.getStackTrace());
        } catch
        (SQLException sqlex) {
            System.out.println(sqlex);
            System.out.println(sqlex.getStackTrace());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.out.println(ex.getStackTrace());
            System.out.println(ex);
        }
    }
}
