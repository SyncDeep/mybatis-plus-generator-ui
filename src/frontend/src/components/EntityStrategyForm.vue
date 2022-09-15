<template>
  <el-form ref="editForm" :model="form" label-width="180px" size="mini">
    <el-form-item label="상위 클래스명">
      <el-input v-model="form.superEntityClass"></el-input>
    </el-form-item>
    <el-form-item label="상위 Entity 컬럼">
      <el-input v-model="superEntityColumn" style="width: 200px"></el-input>
      <el-button @click="addNewColumn">컬럼 추가</el-button>
      <help-tip
        content="상위 클래스에서 상속된 필드. 지정된 테이블 컬럼은 Entity에 코드를 생성하지 않음 (테이블의 컬럼명)"
      ></help-tip>
      <div style="margin-top: 5px">
        <el-tag
          style="margin-right: 5px"
          v-for="col in form.superEntityColumns"
          :key="col"
          @close="removeColumn(col)"
          closable
          >{{ col }}</el-tag
        >
      </div>
    </el-form-item>
    <el-form-item label="값 자동 설정 규칙">
      <el-input v-model="tableFillCol" style="width: 300px">
        <el-select
          style="width: 120px"
          v-model="tableFillType"
          slot="prepend"
          placeholder="선택하세요"
        >
          <el-option label="insert_update" value="insert_update"></el-option>
          <el-option label="insert" value="insert"></el-option>
          <el-option label="update" value="update"></el-option>
        </el-select>
      </el-input>
      <el-button @click="addNewtableFill">필드 추가</el-button>
      <help-tip
        content="자동으로 값을 설정하기 위한 규칙. 예제：@TableField(fill = FieldFill.INSERT_UPDATE)"
      ></help-tip>
      <div style="margin-top: 5px">
        <el-tag
          style="margin-right: 5px"
          v-for="col in form.tableFills"
          :key="col"
          @close="removeTableFill(col)"
          closable
          >{{ col }}</el-tag
        >
      </div>
    </el-form-item>
    <el-form-item label="Optimistic Lock 필드">
      <el-input v-model="form.versionFieldName"></el-input>
    </el-form-item>
    <el-form-item label="삭제 여부 설정 필드">
      <el-input v-model="form.logicDeleteFieldName"></el-input>
    </el-form-item>
    <el-form-item label="serialVersionUID 생성" placeholder>
      <el-switch v-model="form.entitySerialVersionUID"></el-switch>
    </el-form-item>
    <el-form-item label="필드명 Constant 생성" placeholder>
      <el-switch v-model="form.entityColumnConstant"></el-switch>
      <help-tip
        content="static constant 필드 생성 여부, 예제：public static final String ID = 'test_id'"
      ></help-tip>
    </el-form-item>
    <el-form-item label="ActiveRecord 생성" placeholder>
      <el-switch v-model="form.activeRecord"></el-switch>
    </el-form-item>
    <el-form-item label="Builder 생성" placeholder>
      <el-switch v-model="form.entityBuilderModel"></el-switch>
    </el-form-item>
    <el-form-item label="Lombok annotation" placeholder>
      <el-switch v-model="form.entityLombokModel"></el-switch>
    </el-form-item>
    <el-form-item label="컬럼의 prefix 제거" placeholder>
      <el-switch v-model="form.entityBooleanColumnRemoveIsPrefix"></el-switch>
      <help-tip
        content="예제> 컬럼명: 'is_xxx', 유형: tinyint. 필드명은 'is'는 삭제돼서 xxx로 생성된다"
      ></help-tip>
    </el-form-item>
    <el-form-item label="Generate annotation" placeholder>
      <el-switch v-model="form.entityTableFieldAnnotationEnable"></el-switch>
    </el-form-item>
    <el-form-item label="swagger2 annotation" placeholder>
      <el-switch v-model="form.swagger2"></el-switch>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">저장</el-button>
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
    HelpTip,
  },
  data() {
    return {
      superEntityColumn: "",
      tableFillCol: "",
      tableFillType: "insert",
      form: {
        superEntityClass: "",
        superEntityColumns: [],
        tableFills: [],
        entitySerialVersionUID: false,
        entityColumnConstant: false,
        activeRecord: false,
        entityBuilderModel: false,
        entityLombokModel: false,
        entityBooleanColumnRemoveIsPrefix: false,
        entityTableFieldAnnotationEnable: false,
        versionFieldName: "",
        logicDeleteFieldName: "",
        swagger2: false,
      },
      rules: {},
    };
  },
  watch: {
    userConfig: function () {
      _.assign(this.form, this.userConfig.entityStrategy);
    },
  },
  mounted: function () {
    _.assign(this.form, this.userConfig.entityStrategy);
  },
  methods: {
    addNewColumn() {
      if (!this.form.superEntityColumns) {
        this.form.superEntityColumns = [];
      }
      if (this.superEntityColumn) {
        if (
          this.form.superEntityColumns.indexOf(this.superEntityColumn) === -1
        ) {
          this.form.superEntityColumns.push(this.superEntityColumn);
        }
        this.superEntityColumn = "";
      } else {
        this.$message.warning("Please enter a field name");
      }
    },
    removeColumn(col) {
      if (!this.form.superEntityColumns) {
        return;
      }
      this.form.superEntityColumns.splice(
        this.form.superEntityColumns.indexOf(col),
        1
      );
    },
    addNewtableFill() {
      if (!this.form.tableFills) {
        this.form.tableFills = [];
      }

      if (this.tableFillCol) {
        let tableFill = this.tableFillCol + ":" + this.tableFillType;
        if (this.form.tableFills.indexOf(tableFill) === -1) {
          this.form.tableFills.push(tableFill);
        }
        this.tableFillCol = "";
      } else {
        this.$message.warning("Please enter a field name");
      }
    },
    removeTableFill(col) {
      if (!this.form.tableFills) {
        return;
      }
      this.form.tableFills.splice(this.form.tableFills.indexOf(col), 1);
    },
    onSubmit() {
      axios
        .post("/api/output-file-info/save-entity-strategy", this.form)
        .then((res) => {
          this.$message.success("저장했습니다");
        });
    },
  },
};
</script>
