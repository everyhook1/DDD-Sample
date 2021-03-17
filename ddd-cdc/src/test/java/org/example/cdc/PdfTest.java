/**
 * @(#)PdfTest.java, 3月 01, 2021.
 * <p>
 * Copyright 2021 fenbi.com. All rights reserved.
 * FENBI.COM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.example.cdc;

import com.itextpdf.awt.geom.Rectangle2D.Float;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ContentByteUtils;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfContentStreamProcessor;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liubin01
 */
public class PdfTest {

    @Test
    public void getPosition() throws IOException {
        //1.给定文件
        String path = "/Users/liubin/Desktop/a.pdf";
        File pdfFile = new File(path);
        //2.定义一个byte数组，长度为文件的长度
        byte[] pdfData = FileUtils.readFileToByteArray(pdfFile);
        //3.指定关键字
        String keyword = "居右";

        //4.调用方法，给定关键字和文件
        List<float[]> positions = findKeywordPostions(pdfData, keyword);

        //5.返回值类型是  List<float[]> 每个list元素代表一个匹配的位置，分别为 float[0]所在页码  float[1]所在x轴 float[2]所在y轴
        System.out.println("total:" + positions.size());
        if (positions.size() > 0) {
            for (float[] position : positions) {
                System.out.print("pageNum: " + (int) position[0]);
                System.out.print("\tx: " + position[1]);
                System.out.println("\ty: " + position[2]);
            }
        }
    }

    @Test
    public void test1() {

    }

    public void addStamper(String in,String out) throws DocumentException, IOException {
        String path = "/Users/liubin/Desktop/a.pdf";
        PdfReader reader = new PdfReader(path);
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("/Users/liubin/Desktop/a1.pdf"));
        Font font = new Font();
        font.setSize(7);
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            ColumnText.showTextAligned(stamper.getUnderContent(i), Element.ALIGN_CENTER, new Phrase("hello world", font), 30, 700, 0);
            ColumnText.showTextAligned(stamper.getUnderContent(i), Element.ALIGN_CENTER, new Phrase("hello world", font), 30, 707, 0);
        }
        stamper.close();
    }

    /**
     * findKeywordPostions
     *
     * @param pdfData 通过IO流 PDF文件转化的byte数组
     * @param keyword 关键字
     * @return List<float [ ]> : float[0]:pageNum float[1]:x float[2]:y
     * @throws IOException
     */
    public List<float[]> findKeywordPostions(byte[] pdfData, String keyword) throws IOException {
        List<float[]> result = new ArrayList<>();
        List<PdfPageContentPositions> pdfPageContentPositions = getPdfContentPositionsList(pdfData);


        for (PdfPageContentPositions pdfPageContentPosition : pdfPageContentPositions) {
            List<float[]> charPositions = findPositions(keyword, pdfPageContentPosition);
            if (charPositions.size() < 1) {
                continue;
            }
            result.addAll(charPositions);
        }
        return result;
    }


    private List<PdfPageContentPositions> getPdfContentPositionsList(byte[] pdfData) throws IOException {
        PdfReader reader = new PdfReader(pdfData);


        List<PdfPageContentPositions> result = new ArrayList<>();


        int pages = reader.getNumberOfPages();
        for (int pageNum = 1; pageNum <= pages; pageNum++) {
            PdfRenderListener pdfRenderListener = new PdfRenderListener(pageNum);
            //解析pdf，定位位置
            PdfContentStreamProcessor processor = new PdfContentStreamProcessor(pdfRenderListener);
            PdfDictionary pageDic = reader.getPageN(pageNum);
            PdfDictionary resourcesDic = pageDic.getAsDict(PdfName.RESOURCES);
            try {
                processor.processContent(ContentByteUtils.getContentBytesForPage(reader, pageNum), resourcesDic);
            } catch (IOException e) {
                reader.close();
                throw e;
            }


            String content = pdfRenderListener.getContent();
            List<CharPosition> charPositions = pdfRenderListener.getcharPositions();


            List<float[]> positionsList = new ArrayList<>();
            for (CharPosition charPosition : charPositions) {
                float[] positions = new float[]{charPosition.getPageNum(), charPosition.getX(), charPosition.getY()};
                positionsList.add(positions);
            }


            PdfPageContentPositions pdfPageContentPositions = new PdfPageContentPositions();
            pdfPageContentPositions.setContent(content);
            pdfPageContentPositions.setPositions(positionsList);


            result.add(pdfPageContentPositions);
        }
        reader.close();
        return result;
    }


    private List<float[]> findPositions(String keyword, PdfPageContentPositions pdfPageContentPositions) {


        List<float[]> result = new ArrayList<>();


        String content = pdfPageContentPositions.getContent();
        List<float[]> charPositions = pdfPageContentPositions.getPositions();


        for (int pos = 0; pos < content.length(); ) {
            int positionIndex = content.indexOf(keyword, pos);
            if (positionIndex == -1) {
                break;
            }
            float[] postions = charPositions.get(positionIndex);
            result.add(postions);
            pos = positionIndex + 1;
        }
        return result;
    }


    private static class PdfPageContentPositions {
        private String content;
        private List<float[]> positions;


        public String getContent() {
            return content;
        }


        public void setContent(String content) {
            this.content = content;
        }


        public List<float[]> getPositions() {
            return positions;
        }


        public void setPositions(List<float[]> positions) {
            this.positions = positions;
        }
    }


    private static class PdfRenderListener implements RenderListener {
        private final int pageNum;
        private final StringBuilder contentBuilder = new StringBuilder();
        private final List<CharPosition> charPositions = new ArrayList<>();


        public PdfRenderListener(int pageNum) {
            this.pageNum = pageNum;
        }


        public void beginTextBlock() {
        }


        public void renderText(TextRenderInfo renderInfo) {
            List<TextRenderInfo> characterRenderInfos = renderInfo.getCharacterRenderInfos();
            for (TextRenderInfo textRenderInfo : characterRenderInfos) {
                String word = textRenderInfo.getText();
                Float rectangle = textRenderInfo.getAscentLine().getBoundingRectange();

                float x = (float) rectangle.getX();
                float y = (float) rectangle.getY();

                CharPosition charPosition = new CharPosition(pageNum, x, y);
                charPositions.add(charPosition);
                contentBuilder.append(word);

            }
        }


        public void endTextBlock() {
        }


        public void renderImage(ImageRenderInfo renderInfo) {
        }


        public String getContent() {
            return contentBuilder.toString();
        }


        public List<CharPosition> getcharPositions() {
            return charPositions;
        }
    }


    @Data
    @AllArgsConstructor
    private static class CharPosition {
        private int pageNum;
        private float x;
        private float y;
    }
}
