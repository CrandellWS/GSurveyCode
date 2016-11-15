package gamesurvey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import objects.Response;
import objects.Survey;
import objects.SurveyData;
import objects.VNode;
import objects.result.ResultData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Martin on 20.03.2016.
 */
public class ResponseXLS {

    //http://www.ats.ucla.edu/stat/mult_pkg/faq/general/Excel_file_set_up.htm
    //
    public void generate(List<Response> responseList, Survey survey, OutputStream os) throws IOException {
        Workbook wb = new HSSFWorkbook();
        CreationHelper createHelper = wb.getCreationHelper();
        Sheet sheet = wb.createSheet("gs");

        Row rowTitle=sheet.createRow(0);

        ObjectMapper sMapper = new ObjectMapper();
        SurveyData sd = sMapper.readValue(survey.getJsonData(), SurveyData.class);
        Cell start=rowTitle.createCell(0);
        start.setCellValue("Start");
        HashMap<Integer, Integer> titles=new HashMap<>();
        titles.put(1,0); //start node
        for(int i=0;i<sd.getNodeList().size();i++)
        {
            VNode n=sd.getNodeList().get(i);
            Cell cell=rowTitle.createCell(i+1);
            cell.setCellValue(n.getName());
            titles.put(n.getId(), i+1);
        }

        ObjectMapper mapper = new ObjectMapper();
        for(int i=0;i<responseList.size();i++)
        {
            Response r=responseList.get(i);
            TypeFactory tf = mapper.getTypeFactory();
            ObjectMapper m = new ObjectMapper();
            ResultData rd = m.readValue(r.getResponse(), ResultData.class);
            Row row=sheet.createRow(i+1);
            for (int j=0;j< rd.getValues().size();j++) {
                ResultData.VideoResult entry = rd.getValues().get(j);
                if(entry.getId().length()>0) {
                    int id=Integer.parseInt(entry.getId());
                    Cell cell=row.getCell(j);
                    if(cell==null)
                        cell=row.createCell(j);
                    System.out.println(cell.getColumnIndex() +" "+cell.getRowIndex()+ " "+entry.getValue());

                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    cell.setCellValue(entry.getValue());

                }
            }
        }
        wb.write(os);
        os.close();
    }
}
