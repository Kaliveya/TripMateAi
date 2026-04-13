package com.sean.trip.tripmateai.service.impl;

import com.sean.trip.tripmateai.domain.dto.MdImportRequest;
import com.sean.trip.tripmateai.service.DataImportService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class DataImportServiceImpl implements DataImportService {

    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;

    private static final Logger log = Logger.getLogger(DataImportServiceImpl.class.getName());

    @Override
    public void importMdData(MdImportRequest request) {

        //获取md文件路径
        String mdFilePath = request.getMdFilePath();
//        //解析md文件并切块
//        List<String> chunks = AnalyzeMdUtil.analyzeMd(mdFilePath);
//        //embedding各切块并入库
//        List<TextSegment> segments = new ArrayList<>();
//        for (String chunk : chunks) {
//            // 附加元数据，检索时可用于过滤
//            Metadata metadata = Metadata.from(Map.of(
//                    "city", request.getCity(),
//                    "source", mdFilePath,
//                    "importTime", LocalDateTime.now().toString()
//            ));
//            segments.add(TextSegment.from(chunk, metadata));
//        }

        // 1. 用 LangChain4j 加载 MD 文件
        Document document = FileSystemDocumentLoader.loadDocument(
                Path.of(mdFilePath),
                new TextDocumentParser()  // MD 本质是纯文本
        );
        // 2. 用内置切块器替代 AnalyzeMdUtil
        DocumentBySentenceSplitter splitter = new DocumentBySentenceSplitter(500,50);
        List<TextSegment> segments = splitter.split(document);

        // 3. 给每个 chunk 附加元数据
        String importTime = LocalDateTime.now().toString();
        segments = segments.stream()
                .map(seg -> TextSegment.from(
                        seg.text(),
                        seg.metadata()
                                .put("city", request.getCity())
                                .put("source", mdFilePath)
                                .put("importTime", importTime)
                ))
                .collect(Collectors.toList());

        // 批量embed（比逐条快，减少API调用次数）
        Response<List<Embedding>> embeddingResponse = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = embeddingResponse.content();
        // 批量存入向量库
        embeddingStore.addAll(embeddings, segments);
        log.info("成功导入"+ segments.size() +"个chunk，来源"+ mdFilePath);
    }
}
