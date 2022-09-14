<template>
  <el-form ref="editForm" :model="form" label-width="180px" size="mini">
    <el-form-item label="Entity's superclass name">
      <el-input v-model="form.superEntityClass"></el-input>
    </el-form-item>
    <el-form-item label="Public fields of Entity">
      <el-input v-model="superEntityColumn" style="width: 200px"></el-input>
      <el-button @click="addNewColumn">Add field</el-button>
      <help-tip
        content="Public fields are inherited from the superclass by default. Even if there are related fields in the table, they will not be generated into the Entity (note that you need to configure the original field name of the database, not the property name in the superclass!!)"
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
    <el-form-item label="Fields to be autofilled">
      <el-input v-model="tableFillCol" style="width: 300px">
        <el-select
          style="width: 120px"
          v-model="tableFillType"
          slot="prepend"
          placeholder="Please choose"
        >
          <el-option label="insert_update" value="insert_update"></el-option>
          <el-option label="insert" value="insert"></el-option>
          <el-option label="update" value="update"></el-option>
        </el-select>
      </el-input>
      <el-button @click="addNewtableFill">Add Field</el-button>
      <help-tip
        content="A field with a custom fill will be automatically added to the field, Example：@TableField(fill = FieldFill.INSERT_UPDATE)"
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
    <el-form-item label="Field name that identifies optimistic locking">
      <el-input v-model="form.versionFieldName"></el-input>
    </el-form-item>
    <el-form-item label="Identifies the field name to delete">
      <el-input v-model="form.logicDeleteFieldName"></el-input>
    </el-form-item>
    <el-form-item label="Whether to generate serialVersionUID" placeholder>
      <el-switch v-model="form.entitySerialVersionUID"></el-switch>
    </el-form-item>
    <el-form-item label="Whether to generate field name constants" placeholder>
      <el-switch v-model="form.entityColumnConstant"></el-switch>
      <help-tip
        content="Generate a static constant for each field, Example：public static final String ID = 'test_id'"
      ></help-tip>
    </el-form-item>
    <el-form-item label="Enable ActiveRecord mode" placeholder>
      <el-switch v-model="form.activeRecord"></el-switch>
    </el-form-item>
    <el-form-item label="Enable builder mode" placeholder>
      <el-switch v-model="form.entityBuilderModel"></el-switch>
    </el-form-item>
    <el-form-item label="Enable Lombok annotation" placeholder>
      <el-switch v-model="form.entityLombokModel"></el-switch>
    </el-form-item>
    <el-form-item label="Whether to remove the is prefix of the field" placeholder>
      <el-switch v-model="form.entityBooleanColumnRemoveIsPrefix"></el-switch>
      <help-tip
        content="Example: database field name: 'is_xxx', type: tinyint. When mapping entities, is will be removed, and the final result of mapping in the entity class is xxx"
      ></help-tip>
    </el-form-item>
    <el-form-item label="Whether to force field name annotation" placeholder>
      <el-switch v-model="form.entityTableFieldAnnotationEnable"></el-switch>
    </el-form-item>
    <el-form-item label="Whether to generate swagger2 annotations" placeholder>
      <el-switch v-model="form.swagger2"></el-switch>
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
          this.$message.success("Message saved successfully");
        });
    },
  },
};
</script>
