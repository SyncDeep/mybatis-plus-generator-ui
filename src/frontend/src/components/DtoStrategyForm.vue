<template>
  <el-form ref="editForm" :rules="rules" :model="form" label-width="230px">
    <el-form-item label="Code Author">
      <el-input v-model="form.author" style="width:95%;"></el-input>
    </el-form-item>
    <el-form-item label="Enable Lombok annotations">
      <el-switch v-model="form.enableLombok"></el-switch>
    </el-form-item>
    <el-form-item prop="mapperPackage" label="Mapper(Xml) package name">
      <el-autocomplete
        v-model="form.mapperPackage"
        style="width:95%;"
        :fetch-suggestions="queryMapperPackage"
        @select="handleMapperPackageSelect"
        placeholder="Example：com.example.OrderMapper"
      >
        <el-select
          v-model="form.mapperLocationPrefix"
          style="width:110px;"
          slot="prepend"
          placeholder="Please select the source directory"
        >
          <el-option label="java" value="java"></el-option>
          <el-option label="resources" value="resources"></el-option>
        </el-select>
      </el-autocomplete>
      <help-tip
        content="The package name of Mapper.xml where the generated Mapper method is located, Example：com.example.OrderMapper，selecting java or resources will search for resources in the corresponding source code root directory"
      ></help-tip>
    </el-form-item>
    <el-form-item prop="mapperMethod" label="Mapper(Xml) SQL Node ID">
      <el-input
        style="width:95%;"
        v-model="form.mapperMethod"
        placeholder="Example：selectOrders"
        @blur="handleMapperMethodInput"
      ></el-input>
    </el-form-item>
    <el-form-item label="DTO object that encapsulates query results" prop="fullPackage">
      <el-input
        :clearable="true"
        style="width:95%;"
        v-model="form.fullPackage"
        placeholder="Example：com.example.dto.ExampleResultDto"
      ></el-input>
      <help-tip content="The DTO class used to map the results of the query. If it is empty, Map will be used to encapsulate the query results by default. The complete package and class name must be included. If the class does not exist, it will be automatically generated"></help-tip>
    </el-form-item>
    <el-form-item label="Method for synchronously generating Mapper (java)">
      <el-switch v-model="form.enableCreateDaoMethod"></el-switch>
    </el-form-item>
    <el-form-item label="Whether to enable paging query" v-if="form.enableCreateDaoMethod">
      <el-switch v-model="form.enablePageQuery"></el-switch>
      <help-tip content="After the paging query is enabled, the return value of the Mapper method will be wrapped as a Page type, and the corresponding paging parameters will be added"></help-tip>
    </el-form-item>
    <el-form-item v-if="form.enableCreateDaoMethod" label="Mapper(java) method parameter type">
      <el-radio-group v-model="form.daoMethodParamType">
        <el-radio label="map">Map（SQL dynamic parameters as the key of Map）</el-radio>
        <el-radio label="bean">Java Bean（SQL dynamic parameters as Bean's Property）</el-radio>
        <el-radio label="single">Multiple parameters（SQL dynamic parameters each as a separate method parameter）</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="form.enableCreateDaoMethod && form.daoMethodParamType == 'bean'"
      label="Mapper(java) method parameter DTO"
    >
      <el-input
        style="width:80%;"
        v-model="form.daoMethodParamDto"
        placeholder="Example：com.example.dto.XXXMethodParamsDto"
      ></el-input>
      <help-tip content="The reference position of the method parameter Bean of Mapper, if it does not exist, it will be automatically generated according to the SQL dynamic parameters."></help-tip>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">Generate code</el-button>
    </el-form-item>
  </el-form>
</template>
<script>
import axios from "axios";
import _ from "lodash";
import HelpTip from "@/components/HelpTip";
export default {
  props: ["sql"],
  components: {
    HelpTip,
  },
  data() {
    return {
      showDynamicParamsDemo: false,
      userBasePkg: "",
      form: {
        enableLombok: false,
        enablePageQuery: false,
        enableCreateDaoMethod: false,
        enableParseDynamicParams: true,
        author: "",
        fullPackage: "",
        mapperMethod: "",
        mapperPackage: "",
        mapperLocation: "",
        mapperLocationPrefix: "resources",
        daoMethodParamType: "bean",
        daoMethodParamDto: "",
      },
      rules: {
        mapperPackage: [
          {
            required: true,
            message: "The package name of the Mapper file must be filled",
            trigger: "change",
          },
        ],
        mapperMethod: [
          {
            required: true,
            message: "Mapper method name must be filled",
            trigger: "change",
          },
        ],
      },
    };
  },
  mounted: function () {
    axios.get("/api/sql/basepackage").then((res) => {
      this.userBasePkg = res;
    });
  },
  methods: {
    handleMapperMethodInput() {
      if (this.form.mapperMethod) {
        this.form.fullPackage =
          this.userBasePkg +
          ".dto." +
          _.upperFirst(this.form.mapperMethod) +
          "ResultDto";
        this.form.daoMethodParamDto =
          this.userBasePkg +
          ".dto." +
          _.upperFirst(this.form.mapperMethod) +
          "ParamDto";
      }
    },
    queryMapperPackage(queryString, cb) {
      axios
        .get("/api/ac/mapperxml", {
          params: {
            mapperLocationPrefix: this.form.mapperLocationPrefix,
            searchKey: this.form.mapperPackage,
          },
        })
        .then((res) => {
          if (res) {
            let options = res.map((v) => {
              return { value: v };
            });
            cb(options);
          }
        });
    },
    handleMapperPackageSelect(op) {
      this.form.mapperPackage = op.value;
    },
    onSubmit() {
      this.$refs["editForm"].validate((valid) => {
        if (valid) {
          this.form.mapperLocation =
            this.form.mapperPackage + "." + this.form.mapperMethod;
          axios
            .post("/api/sql/gen-mapper-method", {
              sql: this.sql,
              config: this.form,
            })
            .then((res) => {
              this.$message.success("The code has been successfully generated");
              this.$emit("done");
            });
        } else {
          return false;
        }
      });
    },
  },
};
</script>
