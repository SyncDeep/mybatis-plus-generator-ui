<template>
  <el-form ref="editForm" :model="form" label-width="180px" size="mini">
    <el-form-item label="상위 클래스">
      <el-input v-model="form.superControllerClass"></el-input>
    </el-form-item>
    <el-form-item label="REST annotation" placeholder>
      <el-switch v-model="form.restControllerStyle"></el-switch>
      <help-tip content="@Controller를 @RestController로 변경"></help-tip>
    </el-form-item>
    <el-form-item label="Url Format 변환" placeholder>
      <el-switch v-model="form.controllerMappingHyphenStyle"></el-switch>
      <help-tip content="CamelCase -> hyphen. 예제：/managerUserActionHistory -> /manager-user-action-history"></help-tip>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">Save</el-button>
    </el-form-item>
  </el-form>
</template>
<script>
import axios from "axios";
import _ from "lodash";
import HelpTip from "@/components/HelpTip";
export default {
  props: ["userConfig"],
  components: {
    HelpTip
  },
  data() {
    return {
      form: {
        superControllerClass: "",
        restControllerStyle: false,
        controllerMappingHyphenStyle: false
      },
      rules: {}
    };
  },
  watch: {
    userConfig: function() {
      _.assign(this.form, this.userConfig.controllerStrategy);
    }
  },
  mounted: function() {
    _.assign(this.form, this.userConfig.controllerStrategy);
  },
  methods: {
    onSubmit() {
      axios
        .post("/api/output-file-info/save-controller-strategy", this.form)
        .then(res => {
          this.$message.success("저장했습니다");
        });
    }
  }
};
</script>