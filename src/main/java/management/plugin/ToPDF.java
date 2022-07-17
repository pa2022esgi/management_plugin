package management.plugin;

import com.itextpdf.text.*;
import com.itextpdf.text.html.WebColors;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ToPDF {

    public String getPluginName() {
        return "Project to PDF plugin";
    }

    private static final Font title = new Font(Font.FontFamily.HELVETICA,25);
    private static final Font subTitle = new Font(Font.FontFamily.HELVETICA,15, Font.UNDERLINE);

    private static final Font todo = new Font(Font.FontFamily.HELVETICA,12, Font.NORMAL, WebColors.getRGBColor("#FC9674"));
    private static final Font ongoing = new Font(Font.FontFamily.HELVETICA,12, Font.NORMAL, WebColors.getRGBColor("#2196F3"));
    private static final Font finished = new Font(Font.FontFamily.HELVETICA,12, Font.NORMAL, WebColors.getRGBColor("#4CAF50"));

    public void toPdf(JSONObject project) {
        Document document = new Document();

        try {
            File file = new File("./pdf/result.pdf");
            file.getParentFile().mkdirs();
            file.createNewFile();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            if (project.has("name") && project.get("name") instanceof String && project.has("token") && project.get("token") instanceof String) {
                document.add(new Paragraph(project.getString("name") + " - " + project.getString("token"), title));
            }

            if (project.has("created_at") && project.get("created_at") instanceof String) {
                String date = new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'").parse(project.getString("created_at")));
                document.add(new Paragraph("Créé le " + date));
            }

            document.add( Chunk.NEWLINE );

            if (project.has("description") && project.get("description") instanceof String) {
                document.add(new Paragraph(project.getString("description")));
            }

            document.add( Chunk.NEWLINE );

            if (project.has("users") && project.get("users") instanceof JSONArray && project.getJSONArray("users").length() > 0) {
                document.add(new Paragraph("Liste des utilisateurs :", subTitle));
                document.add( Chunk.NEWLINE );

                for (int i = 0; i < project.getJSONArray("users").length(); i++) {
                    JSONObject user = project.getJSONArray("users").getJSONObject(i);

                    addUser(user, document);
                }
            }

            if (project.has("cards") && project.get("cards") instanceof JSONArray && project.getJSONArray("cards").length() > 0) {
                document.add( Chunk.NEWLINE );
                document.add(new Paragraph("Liste des taches :", subTitle));
                document.add( Chunk.NEWLINE );

                for (int i = 0; i < project.getJSONArray("cards").length(); i++) {
                    JSONObject task = project.getJSONArray("cards").getJSONObject(i);

                    if (i != 0) {
                        LineSeparator separator = new LineSeparator();
                        document.add(new Chunk(separator));
                    }

                    addTask(task, document);
                }
            }

            document.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addUser(JSONObject user, Document document) throws DocumentException {
        if (user.has("email") && user.get("email") instanceof String) {
            String line = "- " + user.getString("email");

            if (user.has("pivot") && user.get("pivot") instanceof JSONObject && user.getJSONObject("pivot").has("banished") && user.getJSONObject("pivot").get("banished") instanceof Number) {
                if (user.getJSONObject("pivot").getNumber("banished").equals(1)) {
                    line += " | banni : oui";
                } else {
                    line += " | banni : non";
                }
            }
            document.add(new Paragraph(line));
        }
    }

    public static void addTask(JSONObject task, Document document) throws DocumentException, ParseException {

        if (task.has("title") && task.get("title") instanceof String){
            document.add(new Paragraph(task.getString("title")));
        }
        document.add(Chunk.NEWLINE);

        if (task.has("labels") && task.get("labels") instanceof JSONArray && task.getJSONArray("labels").length() > 0) {
            String labels = "";
            for (int i = 0; i < task.getJSONArray("labels").length(); i++) {
                JSONObject label = task.getJSONArray("labels").getJSONObject(i);

                if (label.has("name") && label.get("name") instanceof String) {
                    if (!labels.equals("")) {
                        labels += ", " + label.getString("name");
                    } else {
                        labels += label.getString("name");
                    }
                }
            }

            document.add(new Paragraph("Labels : " + labels));
        }

        if (task.has("user") && task.get("user") instanceof JSONObject && task.getJSONObject("user").has("email")) {
            document.add(new Paragraph("Assignée à : " + task.getJSONObject("user").getString("email")));
        }

        if (task.has("due_date") && task.get("due_date") instanceof String) {
            String date = new SimpleDateFormat("dd/MM/yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(task.getString("due_date")));
            document.add(new Paragraph("Pour le : " + date));
        }

        if (task.has("status_id")) {
            String tmp = "";
            Font font = ongoing;

            if (task.getNumber("status_id").equals(1)) {
                tmp = "A FAIRE";
                font = todo;
            } else if (task.getNumber("status_id").equals(2)) {
                tmp = "EN COURS";
            } else if (task.getNumber("status_id").equals(3)) {
                tmp = "FINI";
                font = finished;
            }

            document.add(new Paragraph("Statut : " + tmp, font));
        }

        if (task.has("description") && task.get("description") instanceof String) {
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph(task.getString("description")));
        }
    }

}
