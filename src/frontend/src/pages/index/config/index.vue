<template>
  <div>
    <div class="content-header"></div>
    <div class="content">
      <div class="container">
        <div class="pull-right" style="margin-bottom:10px;">
          <el-button type="primary" @click="addNew()">추가</el-button>
        </div>
        <el-table :data="userConfig.outputFiles" border style="width: 100%">
          <el-table-column prop="fileType" label="파일 유형"></el-table-column>
          <el-table-column width="500" prop="outputLocation" label="파일 경로"></el-table-column>
          <el-table-column label="템플릿">
            <template slot-scope="scope">
              <a href="javascript:;" @click="download(scope.row.fileType, scope.row.templateName)">
                <i class="fa fa-paperclip"></i>
                {{scope.row.templateName}}
              </a>
            </template>
          </el-table-column>
          <el-table-column label="기본 템플릿" width="100">
            <template slot-scope="scope">
              <span v-if="scope.row.builtIn" style="color:red;">Y</span>
              <span v-else>N</span>
            </template>
          </el-table-column>
          <el-table-column label="작업">
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
          <el-tab-pane label="기본 설정" name="base">
            <el-form ref="editForm" :rules="rules" :model="form" label-width="120px">
              <el-form-item
                label="유형"
                prop="fileType"
                placeholder="생성될 유형( Service, Controller, Dao etc)"
              >
                <el-input v-model="form.fileType" :readonly="isUpdate"></el-input>
              </el-form-item>
              <el-form-item label="패키지명" prop="outputLocationPkg" placeholder="예제：org.example.entity">
                <el-input v-model="form.outputLocationPkg" placeholder="org.example.entity">
                  <el-select
                    v-model="form.outputLocationPrefix"
                    style="width:110px;"
                    slot="prepend"
                    placeholder="생성 폴더 선택"
                  >
                    <el-option label="java" value="java"></el-option>
                    <el-option label="resources" value="resources"></el-option>
                  </el-select>
                </el-input>
                <help-tip
                  content="생성된 파일이 저장될 위치 지정. 패지지가 example.mapper인 경우 생성된 파일은 /resources/example/mapper 폴더 저장"
                ></help-tip>
              </el-form-item>
              <el-form-item label="파일 템플릿" prop="templateName">
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
                  <el-button slot="trigger" size="small" type="primary">파일 선택</el-button>
                  <div slot="tip" class="el-upload__tip">.btl 파일</div>
                </el-upload>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="onSubmit">저장</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane label="생성 규칙 설정" name="strategy" v-if="form.builtIn">
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
          { required: true, message: "파일 유형을 입력하세요", trigger: "change" },
        ],
        outputLocationPkg: [
          {
            required: true,
            message: "페이지명을 입력하세요",
            trigger: "change",
          },
        ],
        templateName: [
          { required: true, message: "템플릿을 업로드하세요", trigger: "change" },
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
        this.$message.error("Beetl 템플릿을 선택하세요(.btl)");
        return false;
      }
      if (!isLt2M) {
        this.$message.error("템플릿 파일 최대 용량은 2MB입니다.");
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
            this.$message.success("저장했습니다");
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
      this.$confirm("삭제 하시겠습니까?", "작업 결과", {
        type: "warning",
      }).then(() => {
        axios.post("/api/output-file-info/delete", fileInfo).then((res) => {
          this.$message({
            message: "파일을 삭제 했습니다",
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
        this.$message.success("템플릿을 업로드 했습니다");
        this.form.templateName = res.data.templateName;
        this.form.templatePath = res.data.templatePath;
        this.$refs.upload.clearFiles();
      } else {
        this.$message.error("템플릿 업로드 실패 했습니다");
      }
    },
  },
};
</script>
<style></style>
