<template>
  <div>
    <div class="content-header"></div>
    <div class="content">
      <div class="container">
        <div style="margin-bottom: 10px">
          <el-input
            clearable
            style="width: 300px; margin-right: 20px"
            placeholder="Please enter the table name to query"
            prefix-icon="el-icon-search"
            v-model="searchKey"
          ></el-input>
          <el-button type="primary" @click="openGenSetting">代码生成</el-button>
          <span style="color: #e6a23c; font-size: 14px" v-if="isNewProject"
            >The current project has not saved the custom configuration, the default configuration will be used. You can also <a
              href="#"
              @click="openImportProjectView"
              >import</a
            >Configuration of other projects, or custom
            <router-link to="/config">Configure</router-link>
          </span>
        </div>
        <el-table
          ref="tableList"
          :data="queryTables"
          border
          style="width: 100%"
          @selection-change="handleTableSelection"
        >
          <el-table-column type="selection" width="55"></el-table-column>
          <el-table-column
            type="index"
            width="50"
            label="Serial numbe"
          ></el-table-column>
          <el-table-column sortable prop="name" label="table name">
            <template slot-scope="scope">{{ scope.row.name }}</template>
          </el-table-column>
          <el-table-column prop="comment" label="table comment"></el-table-column>
        </el-table>
      </div>
    </div>
    <el-dialog
      :visible.sync="showGenSettingDialog"
      title="Output Configuration"
      width="70%"
      top="5vh"
    >
      <el-form label-width="220px">
        <el-form-item label="Code Author">
          <el-input v-model="genSetting.author" style="width: 260px"></el-input>
        </el-form-item>
        <el-form-item label="Module name">
          <el-input
            v-model="genSetting.moduleName"
            placeholder="The module name will be added to the output package name to separate different modules "
            style="width: 400px"
          ></el-input>
        </el-form-item>
        <el-form-item label="The file to be generated this time">
          <el-checkbox-group v-model="genSetting.choosedOutputFiles">
            <el-checkbox
              v-for="file in userConfig.outputFiles"
              :label="file.fileType"
              :key="file.fileType"
              >{{ file.fileType }}</el-checkbox
            >
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="Controller method to be generated" v-if="isControllerChecked">
          <el-alert
            title="Note: If the template of the Controller is changed, the following options may not take effect and need to be implemented in the template by yourself"
            type="warning"
          ></el-alert>
          <el-checkbox-group v-model="genSetting.choosedControllerMethods">
            <el-checkbox label="list" key="list">List query</el-checkbox>
            <el-checkbox label="getById" key="getById">Query by ID</el-checkbox>
            <el-checkbox label="create" key="create">Add</el-checkbox>
            <el-checkbox label="update" key="update">Update</el-checkbox>
            <el-checkbox label="delete" key="delete">Delete</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="Whether to overwrite if there is a file with the same name">
          <el-switch v-model="genSetting.override"></el-switch>
        </el-form-item>
        <el-form-item label="Target item root directory">
          <el-input
            v-model="genSetting.rootPath"
            style="width: 400px"
          ></el-input>
          <help-tip
            content="The final generated code location: the root directory of the target project + the package directory set by the source code of a specific category"
          ></help-tip>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="genCode()">Start generation</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
    <el-dialog
      :visible.sync="showSavedProjectsDialog"
      title="Project Configuration Import"
      width="50%"
      top="5vh"
    >
      <el-table :data="savedProjects" height="300px">
        <el-table-column label="Project Package Path">
          <template slot-scope="scope">
            {{ scope.row }}
          </template>
        </el-table-column>
        <el-table-column label="导入">
          <template slot-scope="scope">
            <el-button
              type="info"
              size="mini"
              @click="importProjectConfig(scope.row)"
              >Import</el-button
            >
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>
<script>
import axios from "axios";
import _ from "lodash";
import { Loading } from "element-ui";
import HelpTip from "@/components/HelpTip";
export default {
  props: [],
  components: {
    HelpTip,
  },
  data() {
    return {
      searchKey: "",
      showGenSettingDialog: false,
      tables: [],
      choosedTables: [],
      genSetting: {
        author: "",
        choosedOutputFiles: [],
        override: false,
        moduleName: "",
        choosedControllerMethods: [],
        rootPath: "",
      },
      userConfig: {},
      outputFileInfos: [],
      isNewProject: false,
      showSavedProjectsDialog: false,
      savedProjects: [],
    };
  },
  computed: {
    isControllerChecked: function () {
      if (this.genSetting.choosedOutputFiles) {
        if (this.genSetting.choosedOutputFiles.indexOf("Controller") !== -1) {
          return true;
        } else {
          return false;
        }
      }
      return false;
    },
    queryTables: function () {
      let tmp = [];
      this.tables.forEach((t, i) => {
        if (
          t.name &&
          (t.name.toLowerCase().indexOf(this.searchKey.toLowerCase()) !== -1 ||
            !this.searchKey)
        ) {
          tmp.push(t);
        }
      });
      return tmp;
    },
  },
  mounted: function () {
    this.getAllTables();
    this.getUserConfig();
    axios.get("/api/output-file-info/check-if-new-project").then((res) => {
      this.isNewProject = res;
    });
  },
  methods: {
    handleTableSelection(val) {
      this.choosedTables = val.map((t) => t.name);
    },
    getAllTables() {
      axios.get("/api/db/tables").then((res) => {
        this.tables = res;
      });
    },
    getUserConfig() {
      axios.get("/api/output-file-info/user-config").then((res) => {
        this.userConfig = res;
      });
    },
    openGenSetting() {
      if (this.choosedTables.length === 0) {
        this.$message.warning("Please select at least one data table ");
        return;
      }
      //Get the last build configuration
      try {
        let lastSetting = sessionStorage.getItem("gen-setting");
        if (lastSetting) {
          _.assign(this.genSetting, JSON.parse(lastSetting));
          //Clear some one-time configuration
          this.genSetting.moduleName = "";
          this.genSetting.override = false;
        }
        if (!this.genSetting.rootPath) {
          axios.get("/api/output-file-info/project-root-path").then((res) => {
            this.genSetting.rootPath = res;
          });
        }
      } catch (e) {
        console.error(e);
      }
      this.showGenSettingDialog = true;
    },
    genCode() {
      this.$confirm(
        "Confirm to generate selected'" +
          this.choosedTables.length +
          " business code for a data sheet?",
        "Operation",
        {
          type: "warning",
        }
      ).then(() => {
        let setting = JSON.stringify(_.clone(this.genSetting));
        sessionStorage.setItem("gen-setting", setting);
        let params = {};
        params.genSetting = this.genSetting;
        params.tables = this.choosedTables;
        let aLoading = Loading.service();
        axios
          .post("/api/mbp-generator/gen-code", params)
          .then((res) => {
            this.$message({
              message: "Business code has been generated",
              type: "success",
            });
            aLoading.close();
            this.showGenSettingDialog = false;
            this.choosedTables = [];
            this.$refs.tableList.clearSelection();
          })
          .catch(() => {
            aLoading.close();
          });
      });
    },
    openImportProjectView() {
      this.showSavedProjectsDialog = true;
      axios.get("/api/output-file-info/all-saved-project").then((res) => {
        this.savedProjects = res;
      });
    },
    importProjectConfig(sourceProjectPkg) {
      this.$confirm("Are you sure about importing " + sourceProjectPkg + "Configuration？")
        .then(() => {
          axios
            .post(
              "/api/output-file-info/import-project-config/" + sourceProjectPkg
            )
            .then(() => {
              this.$message.success("Configuration imported");
              this.showSavedProjectsDialog = false;
              setTimeout(() => {
                window.location.reload();
              }, 2);
            });
        })
        .catch(() => {});
    },
  },
};
</script>
<style></style>
