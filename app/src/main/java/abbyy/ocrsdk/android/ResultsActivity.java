package abbyy.ocrsdk.android;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class ResultsActivity extends Activity {

    String outputPath;
    private LinearLayout contactDetails;
    private BusinessCardContact businessCardContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        contactDetails = (LinearLayout) findViewById(R.id.contact_layout);
        String imageUrl = "unknown";

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            imageUrl = extras.getString("IMAGE_PATH");
            outputPath = extras.getString("RESULT_PATH");
        }

        // Starting recognition process
        new AsyncProcessTask(this).execute(imageUrl, outputPath);
//        updateResults(true);
    }

    public void updateResults(Boolean success) {
        if (!success)
            return;
        try {
            StringBuffer contents = new StringBuffer();
            FileInputStream fis = openFileInput(outputPath);
            try {
                Reader reader = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufReader = new BufferedReader(reader);
                String text = null;
                while ((text = bufReader.readLine()) != null) {
                    contents.append(text).append(System.getProperty("line.separator"));
                }
                String contentString = contents.toString().replaceAll("[^\\x20-\\x7e]", "");
                displayMessage(contentString);
            } finally {
                fis.close();
            }
//            InputStream inputStream = getAssets().open("sample.xml");
        } catch (Exception e) {
            displayMessage("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void displayMessage(String text) {
        contactDetails.post(new MessagePoster(text));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_results, menu);
        return true;
    }

    class MessagePoster implements Runnable {
        public MessagePoster(String message) {
            _message = message;
        }

        public void run() {
            Log.d("MessagePoster", "message is :: " + _message);
            XmlParser(_message);
//            tv.append(_message + "\n");
//            setContentView(tv);
        }

        private final String _message;
    }


    private void XmlParser(String result) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(result));
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            Log.d("Root element :", doc.getDocumentElement().getNodeName());

            NodeList nList = doc.getElementsByTagName("businessCard");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                Log.d("Current Element :", nNode.getNodeName());
                businessCardContact = new BusinessCardContact();
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    NodeList nodeList = eElement.getElementsByTagName("field");
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Element childElement = (Element) nodeList.item(i);
                        Log.d("type is : ", childElement.getAttribute("type"));

                        NodeList valueNodeList = childElement.getElementsByTagName("value");
                        for (int k = 0; k < valueNodeList.getLength(); k++) {
                            Element valueElement = (Element) valueNodeList.item(k);
                            Log.d("Node tag is :: ", valueElement.getTagName());
                            Log.d("Node value is :: ", valueElement.getTextContent());
                            if (childElement.getAttribute("type").equalsIgnoreCase("phone")) {
                                // read values
                                businessCardContact.phone = valueElement.getTextContent();
                                displayPhone();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("fax")) {
                                // fax values
                                businessCardContact.fax = valueElement.getTextContent();
                                displayFaxPhone();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("mobile")) {
                                // fax values
                                businessCardContact.mobile = valueElement.getTextContent();
                                displayMobile();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("email")) {
                                // email values
                                businessCardContact.email = valueElement.getTextContent();
                                displayEmail();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("web")) {
                                // web values
                                businessCardContact.web = valueElement.getTextContent();
                                displayWeb();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("address")) {
                                // address values
                                businessCardContact.address = valueElement.getTextContent();
                                displayAddress();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("name")) {
                                // name values
                                businessCardContact.name = valueElement.getTextContent();
                                displayName();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("company")) {
                                // company values
                                businessCardContact.company = valueElement.getTextContent();
                                displayCompany();
                            } else if (childElement.getAttribute("type").equalsIgnoreCase("job")) {
                                // job values
                                businessCardContact.job = valueElement.getTextContent();
                                displayJob();
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayPhone() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Phone Number");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.phone);
        phone1.setTag("phone");
        contactDetails.addView(phone1);
    }

    private void displayFaxPhone() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Fax");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.fax);
        phone1.setTag("fax");
        contactDetails.addView(phone1);
    }

    private void displayEmail() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Email");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.email);
        phone1.setTag("email");
        contactDetails.addView(phone1);
    }

    private void displayCompany() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Company");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.company);
        phone1.setTag("company");
        contactDetails.addView(phone1);
    }

    private void displayJob() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Job");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.job);
        phone1.setTag("job");
        contactDetails.addView(phone1);
    }

    private void displayWeb() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Web");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.web);
        phone1.setTag("web");
        contactDetails.addView(phone1);
    }

    private void displayAddress() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Address");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.address);
        phone1.setTag("address");
        contactDetails.addView(phone1);
    }

    private void displayName() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Name");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.name);
        phone1.setTag("name");
        contactDetails.addView(phone1);
    }

    private void displayMobile() {
        TextView phoneText = new TextView(this);
        phoneText.setText("Mobile");
        contactDetails.addView(phoneText);
        EditText phone1 = new EditText(this);
        phone1.setText(businessCardContact.mobile);
        phone1.setTag("mobile");
        contactDetails.addView(phone1);
    }


    public class BusinessCardContact {
        private String name, phone, fax, email, web, address, company, job, mobile;
    }

}
