package com.lancer.workflow.modules.activiti.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@RestController
@RequestMapping("/model")
public class ModelController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建模型后，打开流程设计器
     * @param request
     * @param response
     */
    @GetMapping("/create")
    public void create(HttpServletRequest request, HttpServletResponse response) {


            try {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode editorNode = objectMapper.createObjectNode();
                editorNode.put("id", "canvas");
                editorNode.put("resourceId", "canvas");
                ObjectNode stencilSetNode = objectMapper.createObjectNode();
                stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
                editorNode.put("stencilset", stencilSetNode);
                Model modelData = repositoryService.newModel();

                ObjectNode modelObjectNode = objectMapper.createObjectNode();
                modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, "name");
                modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
                modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "description");
                modelData.setMetaInfo(modelObjectNode.toString());
                modelData.setName("name");
                modelData.setKey(StringUtils.defaultString("key"));

                repositoryService.saveModel(modelData);
                repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));

                request.setAttribute("modelId", modelData.getId());

                response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

           /* //初始化一个空模型
           try {
            Model model = repositoryService.newModel();
            //设置一些默认信息
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String key = request.getParameter("key");
            if (name == null || name == ""){
                name = "";
            }
            if (description == null || description == ""){
                description = "";
            }
            if (key == null || key == ""){
                key = "";
            }

            ObjectNode modelNode = objectMapper.createObjectNode();
            modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);

            model.setName(name);
            model.setKey(key);
            model.setMetaInfo(modelNode.toString());

            // 保存模型
            repositoryService.saveModel(model);
            String id = model.getId();

            //完善ModelEditorSource
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");

            // 属性
            ObjectNode propertiesNode = objectMapper.createObjectNode();
            propertiesNode.put("process_id", model.getKey());
            propertiesNode.put("name", model.getName());
            editorNode.set("properties", propertiesNode);

            // 模板
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);

            //添加模型编辑器资源 act_ge_bytearray表会新增一条数据
            repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));

            // 重定向到设计器界面
            response.sendRedirect(request.getContextPath() + "/activiti/modeler.html?modelId=" + id);
            //response.sendRedirect("/modeler.html?modelId=" + id);

        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /*String id = "";
        String name = "出差申请-Travel approval process";
        String description = "出差申请-Travel approval process";
        String key = "travel_approval_process";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        Model modelData = this.repositoryService.newModel();
        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put("name", name);
        modelObjectNode.put("revision", 1);
        description = StringUtils.defaultString(description);
        modelObjectNode.put("description", description);
        modelData.setMetaInfo(modelObjectNode.toString());
        modelData.setName(name);
        modelData.setKey(StringUtils.defaultString(key));
        this.repositoryService.saveModel(modelData);
        this.repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
        id = modelData.getId();

        //一定要输出id，部署需要，刚接触activiti不知道这个id在哪里
        System.out.println("id ========= " + id);
        response.sendRedirect("/modeler.html?modelId=" + id);*/
  }

    /**
     * @Author i
     * @Description 保存流程
     * @Date 11:42 2024/12/06
     * @Param [modelId, name, json_xml, svg_xml, description]
     * @return void
     **/
    @PutMapping(value = { "/{modelId}/save" })
    @ResponseStatus(HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, @RequestParam("name") String name,
                          @RequestParam("json_xml") String json_xml, @RequestParam("svg_xml") String svg_xml,
                          @RequestParam("description") String description) {
        try {
            Model model = this.repositoryService.getModel(modelId);

            ObjectNode modelJson = (ObjectNode) this.objectMapper.readTree(model.getMetaInfo());

            modelJson.put("name", name);
            modelJson.put("description", description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);

            this.repositoryService.saveModel(model);

            this.repositoryService.addModelEditorSource(model.getId(),json_xml.getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            transcoder.transcode(input, output);
            byte[] result = outStream.toByteArray();
            this.repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();
        } catch (Exception e) {
            throw new ActivitiException("Error saving model", e);
        }
    }

}