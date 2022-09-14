<template>
  <div>
    <div class="content-header"></div>
    <div class="content">
      <div class="container">
        <div class="pull-right" style="margin-bottom:10px;">
          <el-button type="primary" @click="addNew()">Add</el-button>
        </div>
        <el-table :data="userConfig.outputFiles" border style="width: 100%">
          <el-table-column prop="fileType" label="Output file type"></el-table-column>
          <el-table-column width="500" prop="outputLocation" label="Output path"></el-table-column>
          <el-table-column label="File Templates">
            <template slot-scope="scope">
              <a href="javascript:;" @click="download(scope.row.fileType, scope.row.templateName)">
                <i class="fa fa-paperclip"></i>
                {{scope.row.templateName}}
              </a>
            </template>
          </el-table-column>
          <el-table-column label="Whether built-in" width="80">
            <template slot-scope="scope">
              <span v-if="scope.row.builtIn" style="color:red;">Y</span>
              <span v-else>N</span>
            </template>
          </el-table-column>
          <el-table-column label="Action">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="info"
                icon="el-icon-edit"
                circle
                @click="editFileInfo(scope.row)"
              ></el-button>
              <el-button
                size="mini"
                circle
                type="warning"
                icon="el-icon-delete"
                v-if="!scope.row.builtIn"
                @click="deleteFileInfo(scope.row)"
              ></el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-dialog :visible.sync="showEditForm" width="70%" top="5vh">
        <el-tabs v-model="activeName">
          <el-tab-pane label="Base configuration" name="base">
            <el-form ref="editForm" :rules="rules" :model="form" label-width="120px">
              <el-form-item
                label="Type ID"
                prop="fileType"
                placeholder="Used to identify the generated files of this class, such as: service interface, Controller, etc."
              >
                <el-input v-model="form.fileType" :readonly="isUpdate"></el-input>
              </el-form-item>
              <el-form-item label="Package name" prop="outputLocationPkg" placeholder="Example：org.example.entity">
                <el-input v-model="form.outputLocationPkg" placeholder="org.example.entity">
                  <el-select
                    v-model="form.outputLocationPrefix"
                    style="width:110px;"
                    slot="prepend"
                    placeholder="Please select the source directory"
                  >
                    <el-option label="java" value="java"></el-option>
                    <el-option label="resources" value="resources"></el-option>
                  </el-select>
                </el-input>
                <help-tip
                  content="The location where the generated file is output, selecting a different source directory will affect the root directory of the output location. For example, selecting resources will use resources as the root directory of the package: example.mapper will be saved to /resources/example/ mapper directory"
                ></help-tip>
              </el-form-item>
              <el-form-item label="File Template" prop="templateName">
                <a
                  href="javascript:;"
                  @click="download(form.fileType, form.templateName)"
                >{{ form.templateName }}</a>
                <el-upload
                  ref="upload"
                  :show-file-list="false"
                  :limit="1"
                  :data="uploadParams"
                  :file-list="uplaodFileList"
                  action="/api/template/upload"
                  :on-success="onUploadSuccess"
                  :before-upload="beforeTemplateUpload"
                >
                  <el-button slot="trigger" size="small" type="primary">Select file</el-button>
                  <div slot="tip" class="el-upload__tip">Only supports .btl files</div>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="onSubmit">Save</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="Strategy Configuration" name="strategy" v-if="form.builtIn">
            <entity-strategy-form v-if="form.fileType=='Entity'" :user-config="userConfig"></entity-strategy-form>
            <mapper-xml-strategy-form v-if="form.fileType=='Mapper.xml'" :user-config="userConfig"></mapper-xml-strategy-form>
            <mapper-strategy-form v-if="form.fileType=='Mapper.java'" :user-config="userConfig"></mapper-strategy-form>
            <service-strategy-form v-if="form.fileType=='Service'" :user-config="userConfig"></service-strategy-form>
            <service-impl-strategy-form
              v-if="form.fileType=='ServiceImpl'"
              :user-config="userConfig"
            ></service-impl-strategy-form>
            <controller-strategy-form v-if="form.fileType=='Controller'" :user-config="userConfig"></controller-strategy-form>
          </el-tab-pane>
        </el-tabs>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import axios from "axios";
import _ from "lodash";
import FileSaver from "file-saver";
import EntityStrategyForm from "@/components/EntityStrategyForm";
import MapperXmlStrategyForm from "@/components/MapperXmlStrategyForm";
import MapperStrategyForm from "@/components/MapperStrategyForm";
import ServiceStrategyForm from "@/components/ServiceStrategyForm";
import ServiceImplStrategyForm from "@/components/ServiceImplStrategyForm";
import ControllerStrategyForm from "@/components/ControllerStrategyForm";
import HelpTip from "@/components/HelpTip";

export default {
  props: [],
  components: {
    EntityStrategyForm,
    MapperStrategyForm,
    MapperXmlStrategyForm,
    ServiceStrategyForm,
    ServiceImplStrategyForm,
    ControllerStrategyForm,
    HelpTip,
  },
  data() {
    return {
      activeName: "base",
      isUpdate: false,
      uplaodFileList: [],
      uploadParams: {
        fileType: "",
      },
      form: {
        fileType: "",
        packageName: "",
        outputLocationPkg: "",
        outputLocationPrefix: "",
        outputLocation: "",
        templateName: "",
        templatePath: "",
        builtIn: false,
      },
      rules: {
        fileType: [
          { required: true, message: "Please enter the file type", trigger: "change" },
        ],
        outputLocationPkg: [
          {
            required: true,
            message: "Please enter the package name to save the file",
            trigger: "change",
          },
        ],
        templateName: [
          { required: true, message: "Please upload the file template", trigger: "change" },
        ],
      },
      userConfig: {},
      showEditForm: false,
      searchKey: "",
      tables: [],
      choosedTables: [],
    };
  },
  mounted: function () {
    this.getFileInfos();
  },
  methods: {
    editFileInfo(fileInfo) {
      this.form = Object.assign(this.form, fileInfo);
      this.isUpdate = true;
      this.uploadParams.fileType = this.form.fileType;
      if (this.form.outputLocation) {
        if (this.form.outputLocation.indexOf(":") !== -1) {
          let temp = this.form.outputLocation.split(":");
          this.form.outputLocationPrefix = temp[0];
          this.form.outputLocationPkg = temp[1];
        } else {
          this.form.outputLocationPkg = this.form.outputLocation;
        }
      }
      //The default root directory of the mapper.xml file is resources, and the other defaults are java
      if (!this.form.outputLocationPrefix) {
        if (this.form.fileType === "Mapper.xml") {
          this.form.outputLocationPrefix = "resources";
        } else {
          this.form.outputLocationPrefix = "java";
        }
      }
      this.showEditForm = true;
    },
    getFileInfos() {
      axios.get("/api/output-file-info/user-config").then((res) => {
        this.userConfig = res;
      });
    },
    clearForm() {
      this.form.fileType = "";
      this.form.outputLocationPrefix = "";
      this.form.outputLocationPkg = "";
      this.form.outputLocation = "";
      this.form.templateName = "";
      this.form.templatePath = "";
      this.form.builtIn = false;
    },
    addNew() {
      this.showEditForm = true;
      this.isUpdate = false;
      this.clearForm();
    },
    beforeTemplateUpload(file) {
      let tempArray = file.name.split(".");
      let fileType = tempArray[tempArray.length - 1];
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (fileType != "btl") {
        this.$message.error("Please select the correct template file format");
        return false;
      }
      if (!isLt2M) {
        this.$message.error("Template file cannot exceed 2MB!");
        return false;
      }
      return true;
    },
    onSubmit() {
      this.$refs["editForm"].validate((valid) => {
        if (valid) {
          this.form.outputLocation =
            this.form.outputLocationPrefix + ":" + this.form.outputLocationPkg;
          axios.post("/api/output-file-info/save", this.form).then((res) => {
            this.$message.success("Message saved successfully");
            this.clearForm();
            this.activeName = "base";
            this.showEditForm = false;
            this.getFileInfos();
          });
        } else {
          return false;
        }
      });
    },
    deleteFileInfo(fileInfo) {
      this.$confirm("Confirm to delete?", "Operation", {
        type: "warning",
      }).then(() => {
        axios.post("/api/output-file-info/delete", fileInfo).then((res) => {
          this.$message({
            message: "The output file has been deleted",
            type: "success",
          });
          this.getFileInfos();
        });
      });
    },
    download(fileType, templateName) {
      axios
        .get("/api/template/download", {
          params: {
            fileType: fileType,
          },
          responseType: "blob",
        })
        .then((response) => {
          FileSaver.saveAs(response, templateName);
        });
    },
    onUploadSuccess(res, file, fileList) {
      if (res.code === 200) {
        this.$message.success("The template file was uploaded successfully");
        this.form.templateName = res.data.templateName;
        this.form.templatePath = res.data.templatePath;
        this.$refs.upload.clearFiles();
      } else {
        this.$message.error("Template file upload failed");
      }
    },
  },
};
</script>
<style></style>
