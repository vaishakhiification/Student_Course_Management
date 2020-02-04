import java.sql.Time;

public class CourseSchedule {
    int CourseId;
    int CourseScheduleId;
    String CourseName;
    Time StartTime;
    Time EndTime;
    //Enums.DayInTheWeek DayInTheWeek;
    int DayInTheWeek;

    CourseSchedule() {
        CourseId = 0;
        CourseScheduleId = 0;
        CourseName = "";
    }

    CourseSchedule(int courseId, int courseScheduleId, String courseName, Time startTime, Time endTime, int day) {
        CourseId = courseId;
        CourseScheduleId = courseScheduleId;
        CourseName = courseName;
        StartTime = startTime;
        EndTime = endTime;
        DayInTheWeek = day;
    }
}
