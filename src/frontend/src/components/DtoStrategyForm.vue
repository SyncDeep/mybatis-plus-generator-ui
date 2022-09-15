<template>
  <el-form ref="editForm" :rules="rules" :model="form" label-width="230px">
    <el-form-item label="작성자">
      <el-input v-model="form.author" style="width:95%;"></el-input>
    </el-form-item>
    <el-form-item label="Lombok annotations">
      <el-switch v-model="form.enableLombok"></el-switch>
    </el-form-item>
    <el-form-item prop="mapperPackage" label="Mapper(Xml) package name">
      <el-autocomplete
        v-model="form.mapperPackage"
        style="width:95%;"
        :fetch-suggestions="queryMapperPackage"
        @select="handleMapperPackageSelect"
        placeholder="예제：com.example.OrderMapper"
      >
        <el-select
          v-model="form.mapperLocationPrefix"
          style="width:110px;"
          slot="prepend"
          placeholder="소스 경로 선택"
        >
          <el-option label="java" value="java"></el-option>
          <el-option label="resources" value="resources"></el-option>
        </el-select>
      </el-autocomplete>
      <help-tip
        content="Mapper.xml의 패키지명으로 생성될 Mapper 파일의 위치. 예제：com.example.OrderMapper，java or resources  + /com/example/OrderMapper.xml"
      ></help-tip>
    </el-form-item>
    <el-form-item prop="mapperMethod" label="Mapper(Xml) SQL Node ID">
      <el-input
        style="width:95%;"
        v-model="form.mapperMethod"
        placeholder="예제：selectOrders"
        @blur="handleMapperMethodInput"
      ></el-input>
    </el-form-item>
    <el-form-item label="DTO 객체" prop="fullPackage">
      <el-input
        :clearable="true"
        style="width:95%;"
        v-model="form.fullPackage"
        placeholder="예제：com.example.dto.ExampleResultDto"
      ></el-input>
      <help-tip content="DTO가 지정되지 않은 경우 Map이 사용됨.  존재하지 않는 경우 자동 생성(package명과 class명 포함)"></help-tip>
    </el-form-item>
    <el-form-item label="고급 설정(java)">
      <el-switch v-model="form.enableCreateDaoMethod"></el-switch>
    </el-form-item>
    <el-form-item label="페이징 쿼리 생성" v-if="form.enableCreateDaoMethod">
      <el-switch v-model="form.enablePageQuery"></el-switch>
      <help-tip content="Mapper는 Page 객체를 리턴하고 페이징 관련 파라미터 자동 추가"></help-tip>
    </el-form-item>
    <el-form-item v-if="form.enableCreateDaoMethod" label="Mapper 파라미터 유형">
      <el-radio-group v-model="form.daoMethodParamType">
        <el-radio label="map">Map</el-radio>
        <el-radio label="bean">Java Bean</el-radio>
        <el-radio label="single">Method Parameter</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item
      v-if="form.enableCreateDaoMethod && form.daoMethodParamType == 'bean'"
      label="메소드 파라미터 DTO"
    >
      <el-input
        style="width:80%;"
        v-model="form.daoMethodParamDto"
        placeholder="예제：com.example.dto.XXXMethodParamsDto"
      ></el-input>
      <help-tip content="파라미터 클래스. 존재하지 않는 경우 자동 생성"></help-tip>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">생성</el-button>
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
            message: "Mapper 페키지 이름을 입력하세요",
            trigger: "change",
          },
        ],
        mapperMethod: [
          {
            required: true,
            message: "Mapper 메소드 이름을 입력하세요",
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
              this.$message.success("코드 생성을 완료 했습니다");
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
