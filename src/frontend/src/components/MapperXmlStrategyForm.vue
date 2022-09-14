<template>
  <el-form ref="editForm" :model="form" label-width="180px" size="mini">
    <el-form-item label="Whether to generate baseResultMap" placeholder>
      <el-switch v-model="form.baseResultMap"></el-switch>
    </el-form-item>
    <el-form-item label="Whether to add the second level cache configuration in xml " placeholder>
      <el-switch v-model="form.enableCache"></el-switch>
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
        baseResultMap: false,
        enableCache: false
      },
      rules: {}
    };
  },
  watch: {
    userConfig: function() {
      _.assign(this.form, this.userConfig.mapperXmlStrategy);
    }
  },
  mounted: function() {
    _.assign(this.form, this.userConfig.mapperXmlStrategy);
  },
  methods: {
    onSubmit() {
      axios
        .post("/api/output-file-info/save-mapper-xml-strategy", this.form)
        .then(res => {
          this.$message.success("Message saved successfully");
        });
    }
  }
};
</script>