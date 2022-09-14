<template>
  <el-form ref="editForm" :model="form" label-width="180px" size="mini">
    <el-form-item label="Controller's superclass name">
      <el-input v-model="form.superControllerClass"></el-input>
    </el-form-item>
    <el-form-item label="Enable REST interface annotation" placeholder>
      <el-switch v-model="form.restControllerStyle"></el-switch>
      <help-tip content="Replace @Controller with @RestController annotation"></help-tip>
    </el-form-item>
    <el-form-item label="Mapping Url Format Conversion" placeholder>
      <el-switch v-model="form.controllerMappingHyphenStyle"></el-switch>
      <help-tip content="CamelCase to hyphen, sample：/managerUserActionHistory -> /manager-user-action-history "></help-tip>
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
          this.$message.success("Message saved successfully");
        });
    }
  }
};
</script>