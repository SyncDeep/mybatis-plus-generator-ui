<template>
  <el-form ref="editForm" :model="form" label-width="180px" size="mini">
    <el-form-item label="Mapper's superclass name">
      <el-input v-model="form.superMapperClass"></el-input>
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
        superMapperClass: ""
      },
      rules: {}
    };
  },
  watch: {
    userConfig: function() {
      _.assign(this.form, this.userConfig.mapperStrategy);
    }
  },
  mounted: function() {
    _.assign(this.form, this.userConfig.mapperStrategy);
  },
  methods: {
    onSubmit() {
      axios
        .post("/api/output-file-info/save-mapper-strategy", this.form)
        .then(res => {
          this.$message.success("Message saved successfully");
        });
    }
  }
};
</script>