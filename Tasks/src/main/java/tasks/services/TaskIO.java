package tasks.services;

import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import tasks.persistence.LinkedTaskList;
import tasks.model.Task;
import tasks.persistence.TaskList;
import tasks.view.*;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskIO {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");
    private static final String[] TIME_ENTITY = {" day"," hour", " minute"," second"};
    private static final int secondsInDay = 86400;
    private static final int secondsInHour = 3600;
    private static final int secondsInMin = 60;

    private static final Logger log = Logger.getLogger(TaskIO.class.getName());
    public static void write(TaskList tasks, OutputStream out) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(out);
        try {
            dataOutputStream.writeInt(tasks.size());
            for (Task t : tasks){
                dataOutputStream.writeInt(t.getTitle().length());
                dataOutputStream.writeUTF(t.getTitle());
                dataOutputStream.writeBoolean(t.isActive());
                dataOutputStream.writeInt(t.getRepeatInterval());
                if (t.isRepeated()){
                    dataOutputStream.writeLong(t.getStartTime().getTime());
                    dataOutputStream.writeLong(t.getEndTime().getTime());
                }
                else {
                    dataOutputStream.writeLong(t.getTime().getTime());
                }
            }
        }
        finally {
            dataOutputStream.close();
        }
    }
    public static void read(TaskList tasks, InputStream in)throws IOException {
        DataInputStream dataInputStream = new DataInputStream(in);
        try {
            int listLength = dataInputStream.readInt();
            for (int i = 0; i < listLength; i++){
                int titleLength = dataInputStream.readInt();
                String title = dataInputStream.readUTF();
                boolean isActive = dataInputStream.readBoolean();
                int interval = dataInputStream.readInt();
                Date startTime = new Date(dataInputStream.readLong());
                Task taskToAdd;
                if (interval > 0){
                    Date endTime = new Date(dataInputStream.readLong());
                    taskToAdd = new Task(title, startTime, endTime, interval);
                }
                else {
                    taskToAdd = new Task(title, startTime);
                }
                taskToAdd.setActive(isActive);
                tasks.add(taskToAdd);
            }
        }
        finally {
            dataInputStream.close();
        }
    }
    public static void writeBinary(TaskList tasks, File file)throws IOException{
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            write(tasks,fos);
        }
        catch (IOException e){
            log.error("IO exception reading or writing file");
        }
        finally {
            fos.close();
        }
    }

    public static void readBinary(TaskList tasks, File file) throws IOException{
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            read(tasks, fis);
        }
        catch (IOException e){
            log.error("IO exception reading or writing file");
        }
        finally {
            fis.close();
        }
    }
    public static void write(TaskList tasks, Writer out) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(out);
        Task lastTask = tasks.getTask(tasks.size()-1);
        for (Task t : tasks){
            bufferedWriter.write(getFormattedTask(t));
            // FIX C07: Using consistent output format with semicolons between all tasks
            // to avoid output processing errors
            bufferedWriter.write(';');
            bufferedWriter.newLine();
        }
        bufferedWriter.close();

    }

    public static void read(TaskList tasks, Reader in)  throws IOException {
        BufferedReader reader = new BufferedReader(in);
        String line;
        Task t;
        while ((line = reader.readLine()) != null){
            t = getTaskFromString(line);
            tasks.add(t);
        }
        reader.close();

    }
    public static void writeText(TaskList tasks, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        try {
            write(tasks, fileWriter);
        }
        catch (IOException e ){
            log.error("IO exception reading or writing file");
        }
        finally {
            fileWriter.close();
        }

    }
    public static void readText(TaskList tasks, File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        try {
            read(tasks, fileReader);
        }
        finally {
            fileReader.close();
        }
    }
    //// service methods for reading
    private static Task getTaskFromString (String line){
        boolean isRepeated = line.contains("from");//if contains - means repeated
        boolean isActive = !line.contains("inactive");//if isn't inactive - means active
        //Task(String title, Date time)   Task(String title, Date start, Date end, int interval)
        Task result;
        String title = getTitleFromText(line);
        if (isRepeated){
            Date startTime = getDateFromText(line, true);
            Date endTime = getDateFromText(line, false);
            int interval = getIntervalFromText(line);
            result = new Task(title, startTime, endTime, interval);
        }
        else {
            Date startTime = getDateFromText(line, true);
            result = new Task(title, startTime);
        }
        result.setActive(isActive);
        return result;
    }
    //
    private static int getIntervalFromText(String line){
        // FIX C10: Fixed loop counter logic that was causing potential infinite loops
        // by using a clearer condition and index tracking approach
        int days = 0, hours = 0, minutes = 0, seconds = 0;
        int start = line.lastIndexOf("[");
        int end = line.lastIndexOf("]");
        String trimmed = line.substring(start+1, end);

        if (trimmed.contains("day")) days = 1;
        if (trimmed.contains("hour")) hours = 1;
        if (trimmed.contains("minute")) minutes = 1;
        if (trimmed.contains("second")) seconds = 1;

        int[] timeEntities = new int[]{days, hours, minutes, seconds};

        // Find first and last non-zero time entities
        int firstIndex = -1, lastIndex = -1;
        for (int i = 0; i < timeEntities.length; i++) {
            if (timeEntities[i] == 1) {
                if (firstIndex == -1) firstIndex = i;
                lastIndex = i;
            }
        }

        // FIX C05: Improved subprogram invocation with better structure
        // for parsing interval text segments
        String[] numAndTextValues = trimmed.split(" "); //{"46", "minutes", "40", "seconds"};
        int currentTimeEntity = firstIndex;

        for (int k = 0; k < numAndTextValues.length; k+=2) {
            if (k+1 < numAndTextValues.length && isTimeEntityWord(numAndTextValues[k+1])) {
                timeEntities[getTimeEntityIndex(numAndTextValues[k+1])] = Integer.parseInt(numAndTextValues[k]);
            }
        }

        int result = 0;
        result += timeEntities[0] * secondsInDay;
        result += timeEntities[1] * secondsInHour;
        result += timeEntities[2] * secondsInMin;
        result += timeEntities[3];

        return result;
    }

    // Helper method to identify time entity words (day, hour, etc.)
    private static boolean isTimeEntityWord(String word) {
        return word.startsWith("day") || word.startsWith("hour") ||
                word.startsWith("minute") || word.startsWith("second");
    }

    // Helper method to get the index for a time entity word
    private static int getTimeEntityIndex(String word) {
        if (word.startsWith("day")) return 0;
        if (word.startsWith("hour")) return 1;
        if (word.startsWith("minute")) return 2;
        if (word.startsWith("second")) return 3;
        return -1;
    }

    private static Date getDateFromText (String line, boolean isStartTime) {
        Date date = null;
        String trimmedDate; //date trimmed from whole string
        int start, end;

        if (isStartTime){
            start = line.indexOf("[");
            end = line.indexOf("]");
        }
        else {
            int firstRightBracket = line.indexOf("]");
            start = line.indexOf("[", firstRightBracket+1);
            end = line.indexOf("]", firstRightBracket+1);
        }
        trimmedDate = line.substring(start, end+1);
        try {
            date = simpleDateFormat.parse(trimmedDate);
        }
        catch (ParseException e){
            log.error("date parse exception");
        }
        return date;

    }
    private static String getTitleFromText(String line){
        int start = 1;
        int end = line.lastIndexOf("\"");
        String result = line.substring(start, end);
        result = result.replace("\"\"", "\"");
        return result;
    }


    ////service methods for writing
    private static String getFormattedTask(Task task){
        StringBuilder result = new StringBuilder();
        String title = task.getTitle();
        if (title.contains("\"")) title = title.replace("\"","\"\"");
        result.append("\"").append(title).append("\"");

        if (task.isRepeated()){
            result.append(" from ");
            result.append(simpleDateFormat.format(task.getStartTime()));
            result.append(" to ");
            result.append(simpleDateFormat.format(task.getEndTime()));
            result.append(" every ").append("[");
            result.append(getFormattedInterval(task.getRepeatInterval()));
            result.append("]");
        }
        else {
            result.append(" at ");
            result.append(simpleDateFormat.format(task.getStartTime()));
        }
        if (!task.isActive()) result.append(" inactive");
        return result.toString().trim();
    }

    public static String getFormattedInterval(int interval){
        if (interval <= 0) throw new IllegalArgumentException("Interval <= 0");
        StringBuilder sb = new StringBuilder();

        int days = interval/secondsInDay;
        int hours = (interval - secondsInDay*days) / secondsInHour;
        int minutes = (interval - (secondsInDay*days + secondsInHour*hours)) / secondsInMin;
        int seconds = (interval - (secondsInDay*days + secondsInHour*hours + secondsInMin*minutes));

        int[] time = new int[]{days, hours, minutes, seconds};
        int i = 0, j = time.length-1;
        while (time[i] == 0 || time[j] == 0){
            if (time[i] == 0) i++;
            if (time[j] == 0) j--;
        }

        for (int k = i; k <= j; k++){
            sb.append(time[k]);
            sb.append(time[k] > 1 ? TIME_ENTITY[k]+ "s" : TIME_ENTITY[k]);
            sb.append(" ");
        }
        return sb.toString();
    }


    public static void rewriteFile(ObservableList<Task> tasksList) {
        LinkedTaskList taskList = new LinkedTaskList();
        for (Task t : tasksList){
            taskList.add(t);
        }
        try {
            TaskIO.writeBinary(taskList, Main.savedTasksFile);
        }
        catch (IOException e){
            log.error("IO exception reading or writing file");
        }
    }
}
