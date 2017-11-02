package com.intelycare.hackathon.Controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

@Controller
public class HomeController {
    private final static Logger logger = LoggerFactory.getLogger(HomeController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String welcome(Model model) throws WriterException, IOException {
        Connection conn = null;
        Statement stmt = null;
        ArrayList<String> sList = new ArrayList<>();
        try {

            String url = "jdbc:mysql://172.16.1.178:3306/iCareHackathon";
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(url, "Username", "Password");
            stmt = conn.createStatement();

            // Result set get the result of the SQL query
            String str = "select provider.id, provider.fname, provider.lname,svcReqStatus.status, svcreqdetail.id as reqdid\n" +
                    "from provider\n" +
                    "join svcprovider on svcprovider.provid = provider.id  and value = 'Accepted'\n" +
                    "join svcreqdetail on svcreqdetail.id = svcprovider.reqdid\n" +
                    "join svcReqStatus on svcReqStatus.reqdid = svcreqdetail.id\n" +
                    "where svcreqdetail.cid = 182 and svcreqdetail.caredate = '2017-10-21'\n" +
                    "group by provider.id";
            System.out.println(str);
            // ResultSet rs = stmt.executeQuery("select * from users where id <10;");
            ResultSet rs = stmt.executeQuery(str);
            int counter = 0;
            while (rs.next()) {
                //Retrieve by column name
                int id = rs.getInt("id");
                String fname = rs.getString("fname");
                String lname = rs.getString("lname");
                //int provid = rs.getInt("provid");
                int reqdid = rs.getInt("reqdid");
                String value = rs.getString("status");
                String fstring = "pid:".concat(String.valueOf(id)).concat("?fname:").concat(fname).concat("?lname:").concat(lname).concat("?reqid:").concat(String.valueOf(reqdid).concat("?status:").concat(String.valueOf(value)));
                //Display value
                System.out.print("Id: " + id);
                System.out.print("FirstName:" + fname);
                //System.out.print("provid: " + String.valueOf(provid));
                System.out.print("ReqddId: " + String.valueOf(reqdid));
                System.out.print("LastName:" + lname);
                System.out.print("LastName:" + lname);


                // Creating QR for Shifts
                String qrCodeText = fstring; //id+":"+fname;
                //String filePath = "/media/sf_SharedFolder/QRCode/QRImage/QRCode_"+String.valueOf(counter)+".jpg";
                String filePath = "/home/vinodh/Documents/IntelyCareHackathon2017/src/main/resources/static/images/" + fname + "_" + String.valueOf(counter) + ".jpg";
                //String filePath = "/home/vinodh/Documents/QRCode/csye6225-fall2017/src/main/resources/static/images/" + fname + "_" + String.valueOf(counter) + ".jpg";
                sList.add(reqdid + "_" + fname + "_" + lname + "_" + String.valueOf(counter) + "_" + String.valueOf(value));
                //sList.add(String.valueOf(value));
                int size = 125;
                String fileType = "png";
                File qrFile = new File(filePath);
                createQRImage(qrFile, qrCodeText, size, fileType);
                counter++;
            }
            rs.close();
            System.out.println("Database connection established");
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.println("Database connection terminated");
                } catch (Exception e) { /* ignore close errors */ }
            }
        }

        model.addAttribute("list", sList);
        return "index";
    }

    private static void createQRImage(File qrFile, String qrCodeText, int size,
                                      String fileType) throws WriterException, IOException {
        // Create the ByteMatrix for the QR-Code that encodes the given String
        Hashtable hintMap = new Hashtable();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix byteMatrix = qrCodeWriter.encode(qrCodeText,
                BarcodeFormat.QR_CODE, size, size, hintMap);
        // Make the BufferedImage that are to hold the QRCode
        int matrixWidth = byteMatrix.getWidth();
        BufferedImage image = new BufferedImage(matrixWidth, matrixWidth,
                BufferedImage.TYPE_INT_RGB);
        image.createGraphics();

        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, matrixWidth, matrixWidth);
        // Paint and save the image using the ByteMatrix
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < matrixWidth; i++) {
            for (int j = 0; j < matrixWidth; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        ImageIO.write(image, fileType, qrFile);
    }
}

