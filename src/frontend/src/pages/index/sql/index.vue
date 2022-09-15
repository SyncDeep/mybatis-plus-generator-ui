<template>
  <div>
    <div class="content-header"></div>
    <div class="content">
      <div class="container">
        <div style="margin-buttom:10px;">코드 생성할 SQL문을 입력하세요</div>
        <el-alert title="Tip：MyBatis의 동적 SQL문을 생성합니다. 조회 조건을 자동 분석해서 Mybatis XML을 생성합니다" type="info" style="margin:10px;" show-icon :closable="false">
          <el-button type="info" @click="showDynamicParamsDemo = true" icon="el-icon-view">예제</el-button>
        </el-alert>
        <codemirror v-model="sqlCode" :options="cmOptions" />
        <el-dialog title="SQL 기반 코드 생성 설정" :visible.sync="showDtoConfig" width="60%" top="5vh">
          <dto-strategy-form :sql="encodedSQL" @done="showDtoConfig = false"></dto-strategy-form>
        </el-dialog>
        <div style="margin-top:10px">
          <el-button type="primary" @click="genDtoFile">코드 생성</el-button>
        </div>
      </div>
      <el-dialog
        :modal="false"
        title="SQL 예제"
        :visible.sync="showDynamicParamsDemo"
        width="80%"
        top="5vh"
      >
        <dynamic-sql-demo></dynamic-sql-demo>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import HelpTip from "@/components/HelpTip";
import { Base64 } from "js-base64";
import DtoStrategyForm from "@/components/DtoStrategyForm";
import DynamicSqlDemo from "@/components/DynamicSqlDemo";

export default {
  components: {
    DynamicSqlDemo,
    HelpTip,
    DtoStrategyForm,
  },
  data() {
    return {
      showDynamicParamsDemo: false,
      showDtoConfig: false,
      sqlCode: "",
      encodedSQL: "",
      cmOptions: {
        tabSize: 4,
        styleActiveLine: true,
        lineNumbers: true,
        line: true,
        mode: "text/x-mysql",
        theme: "solarized light",
      },
    };
  },
  methods: {
    genDtoFile() {
      debugger
      if (!this.sqlCode) {
        this.$message.error("SQL을 입력하세요");
        return;
      }
      this.encodedSQL = Base64.encodeURI(this.sqlCode);
      this.showDtoConfig = true;
    },
  },
};
</script>
