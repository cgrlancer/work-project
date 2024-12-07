/*
package com.lancer.workflow.modules.activiti.controller;


import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lancer.workflow.base.XbootBaseController;
import com.lancer.workflow.common.exception.XbootException;
import com.lancer.workflow.common.utils.PageUtil;
import com.lancer.workflow.common.utils.ResultUtil;
import com.lancer.workflow.common.utils.WrapperUtil;
import com.lancer.workflow.common.vo.PageVo;
import com.lancer.workflow.common.vo.Result;
import com.lancer.workflow.modules.activiti.entity.ActModel;
import com.lancer.workflow.modules.activiti.entity.ActProcess;
import com.lancer.workflow.modules.activiti.service.ActModelService;
import com.lancer.workflow.modules.activiti.service.ActProcessService;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.activiti.editor.constants.ModelDataJsonConstants.*;

*/
/**
 * <p>
 *  模型管理接口
 * </p>
 *
 * @author i
 * @since 2019-04-26
 *//*

@Slf4j
@RestController
@RequestMapping("/oa/actModel")
@Transactional
public class ActModelController extends XbootBaseController<ActModelService, ActModel, String> {

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private ActProcessService actProcessService;

    */
/*@Autowired
    private ActGeBytearrayService actGeBytearrayService;*//*


    */
/**
     * 部署发布模型
     * @param actModel
     * @return
     *//*

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Result<Object> addModel(@ModelAttribute ActModel actModel){

        // 初始化一个空模型
        Model model = repositoryService.newModel();

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(MODEL_NAME, actModel.getName());
        modelNode.put(MODEL_DESCRIPTION, actModel.getDescription());
        modelNode.put(MODEL_REVISION, model.getVersion());

        model.setName(actModel.getName());
        model.setKey(actModel.getModelKey());
        model.setMetaInfo(modelNode.toString());

        // 保存模型
        repositoryService.saveModel(model);
        String id = model.getId();

        // 完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        ObjectNode properties = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.replace("stencilset", stencilSetNode);
        properties.put("process_id", actModel.getModelKey());
        editorNode.replace("properties", properties);

        // 保存扩展模型至数据库
        actModel.setId(id);
        actModel.setVersion(model.getVersion());
        actModel.setCreateTime(new Date());
        service.insert(actModel);
        return new ResultUtil<Object>().setSuccessMsg("添加模型成功");
    }

    */
/**
     * 部署发布模型
     * @param id
     * @return
     *//*

    @RequestMapping(value = "/deploy/{id}", method = RequestMethod.GET)
    public Result<Object> deploy(@PathVariable String id) {

        // 获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return new ResultUtil<Object>().setErrorMsg("模型数据为空，请先成功设计流程并保存");
        }else {
            //检测流程上的条件表达式是否正确
            service.checkModel(bytes);
        }

        try {
            JsonNode modelNode = new ObjectMapper().readTree(bytes);

            BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            if(model.getProcesses().size()==0){
                return new ResultUtil<Object>().setErrorMsg("模型不符合要求，请至少设计一条主线流程");
            }
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

            // 部署发布模型流程
            String processName = modelData.getName() + ".bpmn20.xml";
            Deployment deployment = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .addString(processName, new String(bpmnBytes, "UTF-8"))
                    .deploy();

            // 设置流程分类 保存扩展流程至数据库
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            */
/*System.out.println(list.size());*//*

            int sortNum=0;
            List<ActProcess> actProcesses=actProcessService.selectList(new EntityWrapper<ActProcess>().eq("process_key",modelData.getKey()));
            if(actProcesses!=null&&actProcesses.size()>0){
                sortNum=actProcesses.get(0).getSortNum();
            }else{
                Integer maxSortNum = actProcessService.getMaxSortNum();
                if(maxSortNum!=null){
                    sortNum = maxSortNum.intValue()+1;
                }
            }
            ActModel actModel = service.selectById(id);
            for (ProcessDefinition pd : list) {
                ActProcess actProcess = new ActProcess();
                actProcess.setId(pd.getId());
                actProcess.setName(modelData.getName());
                actProcess.setProcessKey(modelData.getKey());
                actProcess.setDeploymentId(deployment.getId());
                actProcess.setDescription(actModel.getDescription());
                actProcess.setVersion(pd.getVersion());
                actProcess.setXmlName(pd.getResourceName());
                actProcess.setDiagramName(pd.getDiagramResourceName());
                actProcessService.setAllOldByProcessKey(modelData.getKey());
                actProcess.setLatest(true);
                actProcess.setSortNum(sortNum);
                actProcessService.insert(actProcess);
            }
        }catch (Exception e){
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("部署失败");
        }

        return new ResultUtil<Object>().setSuccessMsg("部署成功");
    }

    */
/**
     * 通过文件部署模型
     * @param file
     * @return
     *//*

    @RequestMapping(value = "/deployByFile", method = RequestMethod.POST)
    public Result<Object> deployByFile(@RequestParam MultipartFile file) {

        String fileName = file.getOriginalFilename();
        if (StrUtil.isBlank(fileName)) {
            return new ResultUtil<Object>().setErrorMsg("请先选择文件");
        }

        try {
            InputStream fileInputStream = file.getInputStream();
            Deployment deployment;
            String extension = FilenameUtils.getExtension(fileName);
            String baseName = FilenameUtils.getBaseName(fileName);
            if ("zip".equals(extension) || "bar".equals(extension)) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().name(baseName)
                        .addZipInputStream(zip).deploy();
            } else if ("png".equals(extension)) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(fileName, fileInputStream).deploy();
            } else if (fileName.indexOf("bpmn20.xml") != -1) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(fileName, fileInputStream).deploy();
            } else if ("bpmn".equals(extension)) {
                deployment = repositoryService.createDeployment().name(baseName)
                        .addInputStream(baseName + ".bpmn20.xml", fileInputStream).deploy();
            } else {
                return new ResultUtil<Object>().setErrorMsg("不支持的文件格式");
            }

            // 保存扩展流程至数据库
            Date now = new Date();
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition pd : list) {
                ActProcess actProcess = new ActProcess();
                actProcess.setId(pd.getId());
                actProcess.setName(deployment.getName());
                actProcess.setDeploymentId(deployment.getId());
                actProcess.setProcessKey(pd.getKey());
                actProcess.setVersion(pd.getVersion());
                actProcess.setXmlName(pd.getResourceName());
                actProcess.setDiagramName(pd.getDiagramResourceName());
                actProcessService.setAllOldByProcessKey(pd.getKey());
                actProcess.setLatest(true);
                actProcess.setCreateTime(now);
                //actProcess.setCreateBy();

                int sortNum=0;
                List<ActProcess> actProcesses=actProcessService.selectList(new EntityWrapper<ActProcess>().eq("process_key",pd.getKey()));
                if(actProcesses!=null&&actProcesses.size()>0){
                    sortNum=actProcesses.get(0).getSortNum();
                }else{
                    Integer maxSortNum = actProcessService.getMaxSortNum();
                    if(maxSortNum!=null){
                        sortNum = maxSortNum.intValue()+1;
                    }
                }
                actProcess.setSortNum(sortNum);

                actProcessService.insert(actProcess);
            }
        } catch (Exception e) {
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("部署失败");
        }

        return new ResultUtil<Object>().setSuccessMsg("部署成功");
    }

    */
/**
     * 导出模型XML
     * @param id
     * @param response
     *//*

    @RequestMapping(value = "/export/{id}", method = RequestMethod.GET)
    public void export(@PathVariable String id, HttpServletResponse response) {

        try {
            Model modelData = repositoryService.getModel(id);
            // 获取节点信息
            byte[] nodeBytes = repositoryService.getModelEditorSource(modelData.getId());
            if (nodeBytes == null) {
                throw new XbootException("导出失败，模型数据为空");
            }
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(nodeBytes);
            // 将节点信息转换为xml
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);

            String filename = modelData.getName() + ".bpmn20.xml";
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));

            IOUtils.copy(in, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e){
            e.printStackTrace();
            log.error(e.toString());
            throw new XbootException("导出模型出错");
        }
    }

    */
/**
     * 修改模型
     * @param id
     * @param name
     * @param description
     * @param json_xml
     * @return
     *//*

    @RequestMapping(value = "/edit/{id}", method = RequestMethod.POST)
    public Result<Object> edit(@PathVariable String id,
                               @RequestParam String name, @RequestParam String description,
                               @RequestParam String json_xml) {

        try {
            Model model = repositoryService.getModel(id);

            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

            int newVersion = model.getVersion()+1;
            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            modelJson.put(MODEL_REVISION, newVersion);

            model.setMetaInfo(modelJson.toString());
            model.setName(name);
            model.setVersion(newVersion);
            repositoryService.saveModel(model);

            repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));

//            InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
//            TranscoderInput input = new TranscoderInput(svgStream);
//            //利用Batik将SVG文档转换为PNG
//            PNGTranscoder transcoder = new PNGTranscoder();
//            // Setup output
//            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
//            TranscoderOutput output = new TranscoderOutput(outStream);
//
//            // Do the transformation
//            transcoder.transcode(input, output);
//            final byte[] result = outStream.toByteArray();
//            repositoryService.addModelEditorSourceExtra(model.getId(), result);
//            outStream.close();

            // 更新数据库
            ActModel actModel = service.selectById(id);
            // 更新key
            String key = StrUtil.subBetween(json_xml, "\"process_id\":\"", "\",\"name\"");
            actModel.setModelKey(key);
            actModel.setName(name);
            actModel.setDescription(description);
            actModel.setVersion(newVersion);
            service.updateById(actModel);
        } catch (Exception e) {
            log.error(e.toString());
            return new ResultUtil<Object>().setErrorMsg("修改失败");
        }

        return new ResultUtil<Object>().setSuccessMsg("修改成功");
    }

    */
/**
     * 多条件分页获取模型
     * @param actModel
     * @param pageVo
     * @return
     *//*

    @RequestMapping(value = "/getByCondition",method = RequestMethod.GET)
    public Result<Page<ActModel>> getFileList(@ModelAttribute ActModel actModel,
                                              @ModelAttribute PageVo pageVo){
        Wrapper<ActModel> wrapper = new EntityWrapper<>();

        WrapperUtil.like(wrapper, new String[]{"name", "model_key" }, actModel.getName(), actModel.getModelKey());

        Page<ActModel> page = service.selectPage(PageUtil.initPage(pageVo), wrapper);
        //Page<ActModel> page = service.findByCondition(actModel, PageUtil.initPage(pageVo));
        return new ResultUtil<Page<ActModel>>().setData(page);
    }

    */
/**
     * 通过id批量删除模型
     * @param ids
     * @return
     *//*

    @RequestMapping(value = "/delByIds/{ids}",method = RequestMethod.DELETE)
    public Result<Object> delByIds(@PathVariable String[] ids){

        for(String id :ids){
            repositoryService.deleteModel(id);
            service.deleteById(id);
        }
        return new ResultUtil<Object>().setSuccessMsg("删除成功");
    }

    */
/**
     * 通过id获取模型详情
     * @param id
     * @return
     *//*

    @RequestMapping(value = "/getDetail/{id}",method = RequestMethod.GET)
    public Result<Object> getDetail(@PathVariable String id){
        ActModel actModel = service.selectById(id);
        //Model model = repositoryService.getModel(id);
        byte[] source = repositoryService.getModelEditorSource(id);
        byte[] extra = repositoryService.getModelEditorSourceExtra(id);

        JSONObject result = new JSONObject();
        result.put("actModel", actModel);
        result.put("json", new String(source, Charset.forName("utf-8")));
        result.put("svg", new String(extra, Charset.forName("utf-8")));

        return new ResultUtil<Object>().setData(result);
    }
}
*/
