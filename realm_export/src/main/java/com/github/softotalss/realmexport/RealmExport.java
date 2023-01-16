/*
 * Copyright (c) 2018-present, Softotalss, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 *
 * Part of this code is based on project: https://github.com/wickedev/stetho-realm/
 */

package com.github.softotalss.realmexport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmFieldType;
import io.realm.RealmModel;
import io.realm.RealmResults;
import io.realm.internal.OsList;
import io.realm.internal.OsSharedRealm;
import io.realm.internal.RealmObjectProxy;
import io.realm.internal.Row;
import io.realm.internal.Table;

public class RealmExport {

    private static final String TABLE_PREFIX = "class_";
    private static final String NULL = "[null]";

    private final RealmConfiguration configuration;
    private final String nullValue;
    private final DateFormat dateTimeFormatter;

    private RealmExport(RealmConfiguration configuration, String nullValue, DateFormat dateTimeFormatter) {
        this.configuration = configuration;
        this.nullValue = nullValue;
        this.dateTimeFormatter =  (dateTimeFormatter != null) ? dateTimeFormatter :
                SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG);
    }

    @SuppressWarnings("WeakerAccess")
    public static RealmExport init(RealmConfiguration configuration, String nullValue, DateFormat dateTimeFormatter) {
        return new RealmExport(configuration, nullValue, dateTimeFormatter);
    }

    @SuppressWarnings("WeakerAccess")
    public static RealmExport init(RealmConfiguration configuration, String nullValue) {
        return init(configuration, nullValue, null);
    }

    public static RealmExport init(RealmConfiguration configuration) {
        return init(configuration, NULL);
    }

    @SuppressWarnings({"ConstantConditions", "TryFinallyCanBeTryWithResources"})
    public JsonObject toJson() {
        JsonObject dbJson = new JsonObject();

        final OsSharedRealm sharedRealm = OsSharedRealm.getInstance(configuration, OsSharedRealm.VersionID.LIVE);
        try {
            for (String tableName : sharedRealm.getTablesNames()) {
                if (tableName.startsWith(TABLE_PREFIX)) {
                    dbJson.add(tableName, flattenRowsJson(sharedRealm.getTable(tableName)));
                }
            }
        } finally {
            sharedRealm.close();
        }

        return dbJson;
    }

    public void toJsonFile(String path) throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path), true));
        try {
            writer.write(toJson().toString());
        } finally {
            writer.flush();
            writer.close();
        }
    }

    private enum RealmExportFieldType {
        INTEGER(RealmFieldType.INTEGER),
        BOOLEAN(RealmFieldType.BOOLEAN),
        STRING(RealmFieldType.STRING),
        BINARY(RealmFieldType.BINARY),
        UNSUPPORTED_TABLE(5),
        UNSUPPORTED_MIXED(6),
        UNSUPPORTED_DATE(7),
        DATE(RealmFieldType.DATE),
        FLOAT(RealmFieldType.FLOAT),
        DOUBLE(RealmFieldType.DOUBLE),
        OBJECT(RealmFieldType.OBJECT),
        LIST(RealmFieldType.LIST),

        INTEGER_LIST(RealmFieldType.INTEGER_LIST),
        BOOLEAN_LIST(RealmFieldType.BOOLEAN_LIST),
        STRING_LIST(RealmFieldType.STRING_LIST),
        BINARY_LIST(RealmFieldType.BINARY_LIST),
        DATE_LIST(RealmFieldType.DATE_LIST),
        FLOAT_LIST(RealmFieldType.FLOAT_LIST),
        DOUBLE_LIST(RealmFieldType.DOUBLE_LIST),

        UNKNOWN(null);

        private final RealmFieldType realmFieldType;
        private final int nativeValue;

        RealmExportFieldType(int nativeValue) {
            this.realmFieldType = null;
            this.nativeValue = nativeValue;
        }

        RealmExportFieldType(RealmFieldType realmFieldType) {
            this.realmFieldType = realmFieldType;
            this.nativeValue = realmFieldType == null ? -1 : realmFieldType.getNativeValue();
        }
    }

    private static class RowFetcher {
        private static final RowFetcher sInstance = new RowFetcher();

        static RowFetcher getInstance() {
            return sInstance;
        }

        RowFetcher() {}

        Row getRow(Table targetTable, long index) {
            return targetTable.getCheckedRow(index);
        }
    }

    private static class RowWrapper {
        static RowWrapper wrap(Row row) {
            return new RowWrapper(row);
        }

        private final Row row;

        RowWrapper(Row row) {
            this.row = row;
        }

        long getIndex() {
            return row.getObjectKey();
        }

        RealmExportFieldType getColumnType(long columnIndex) {
            final Enum<?> columnType = row.getColumnType(columnIndex);
            final String name = columnType.name();
            if (name.equals("INTEGER")) {
                return RealmExportFieldType.INTEGER;
            }
            if (name.equals("BOOLEAN")) {
                return RealmExportFieldType.BOOLEAN;
            }
            if (name.equals("STRING")) {
                return RealmExportFieldType.STRING;
            }
            if (name.equals("BINARY")) {
                return RealmExportFieldType.BINARY;
            }
            if (name.equals("UNSUPPORTED_TABLE")) {
                return RealmExportFieldType.UNSUPPORTED_TABLE;
            }
            if (name.equals("UNSUPPORTED_MIXED")) {
                return RealmExportFieldType.UNSUPPORTED_MIXED;
            }
            if (name.equals("UNSUPPORTED_DATE")) {
                return RealmExportFieldType.UNSUPPORTED_DATE;
            }
            if (name.equals("DATE")) {
                return RealmExportFieldType.DATE;
            }
            if (name.equals("FLOAT")) {
                return RealmExportFieldType.FLOAT;
            }
            if (name.equals("DOUBLE")) {
                return RealmExportFieldType.DOUBLE;
            }
            if (name.equals("OBJECT")) {
                return RealmExportFieldType.OBJECT;
            }
            if (name.equals("LIST")) {
                return RealmExportFieldType.LIST;
            }
            if (name.equals("INTEGER_LIST")) {
                return RealmExportFieldType.INTEGER_LIST;
            }
            if (name.equals("BOOLEAN_LIST")) {
                return RealmExportFieldType.BOOLEAN_LIST;
            }
            if (name.equals("STRING_LIST")) {
                return RealmExportFieldType.STRING_LIST;
            }
            if (name.equals("BINARY_LIST")) {
                return RealmExportFieldType.BINARY_LIST;
            }
            if (name.equals("DATE_LIST")) {
                return RealmExportFieldType.DATE_LIST;
            }
            if (name.equals("FLOAT_LIST")) {
                return RealmExportFieldType.FLOAT_LIST;
            }
            if (name.equals("DOUBLE_LIST")) {
                return RealmExportFieldType.DOUBLE_LIST;
            }
            return RealmExportFieldType.UNKNOWN;
        }

        boolean isNull(long columnIndex) {
            return row.isNull(columnIndex);
        }

        boolean isNullLink(long columnIndex) {
            return row.isNullLink(columnIndex);
        }

        long getLong(long columnIndex) {
            return row.getLong(columnIndex);
        }

        boolean getBoolean(long columnIndex) {
            return row.getBoolean(columnIndex);
        }

        float getFloat(long columnIndex) {
            return row.getFloat(columnIndex);
        }

        double getDouble(long columnIndex) {
            return row.getDouble(columnIndex);
        }

        Date getDate(long columnIndex) {
            return row.getDate(columnIndex);
        }

        String getString(long columnIndex) {
            return row.getString(columnIndex);
        }

        byte[] getBinaryByteArray(long columnIndex) {
            return row.getBinaryByteArray(columnIndex);
        }

        long getLink(long columnIndex) {
            return row.getLink(columnIndex);
        }

        OsList getLinkList(long columnIndex) {
            return row.getModelList(columnIndex);
        }

        OsList getValueList(long columnIndex, RealmFieldType fieldType) {
            return row.getValueList(columnIndex, fieldType);
        }
    }

    private Class<? extends RealmModel> getRealmModel(String name) {
        Set<Class<? extends RealmModel>> modelsRM = Realm.getDefaultInstance().getConfiguration().getRealmObjectClasses();
        for (Class<? extends RealmModel> model : modelsRM) {
            if (model.getSimpleName().equals(name)) {
                return model;
            }
        }

        return null;
    }

    private JsonArray flattenRowsJson(Table table) {
        final JsonArray jsonArray = new JsonArray();

        Realm realm = Realm.getDefaultInstance();
        realm.refresh();
        RealmResults<RealmModel> data = (RealmResults<RealmModel>) realm.where(getRealmModel(table.getClassName())).findAll();

        for (RealmModel dataRow : data) {
            Row row = ((RealmObjectProxy) dataRow).realmGet$proxyState().getRow$realm();
            final RowWrapper rowData = RowWrapper.wrap(row);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("index", rowData.getIndex());

            for (String columnName : table.getColumnNames()) {
                long column = table.getColumnKey(columnName);
                switch (rowData.getColumnType(column)) {
                    case INTEGER:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            jsonObject.addProperty(columnName, rowData.getLong(column));
                        }
                        break;
                    case BOOLEAN:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            jsonObject.addProperty(columnName, rowData.getBoolean(column));
                        }
                        break;
                    case STRING:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            jsonObject.addProperty(columnName, rowData.getString(column));
                        }
                        break;
                    case BINARY:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            jsonObject.addProperty(columnName, rowData.getBinaryByteArray(column).toString());
                        }
                        break;
                    case FLOAT:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            final float aFloat = rowData.getFloat(column);
                            if (Float.isNaN(aFloat)) {
                                jsonObject.addProperty(columnName, "NaN");
                            } else if (aFloat == Float.POSITIVE_INFINITY) {
                                jsonObject.addProperty(columnName, "Infinity");
                            } else if (aFloat == Float.NEGATIVE_INFINITY) {
                                jsonObject.addProperty(columnName, "-Infinity");
                            } else {
                                jsonObject.addProperty(columnName, aFloat);
                            }
                        }
                        break;
                    case DOUBLE:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            final double aDouble = rowData.getDouble(column);
                            if (Double.isNaN(aDouble)) {
                                jsonObject.addProperty(columnName, "NaN");
                            } else if (aDouble == Double.POSITIVE_INFINITY) {
                                jsonObject.addProperty(columnName, "Infinity");
                            } else if (aDouble == Double.NEGATIVE_INFINITY) {
                                jsonObject.addProperty(columnName, "-Infinity");
                            } else {
                                jsonObject.addProperty(columnName, aDouble);
                            }
                        }
                        break;
                    case UNSUPPORTED_DATE:
                    case DATE:
                        if (rowData.isNull(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            jsonObject.addProperty(columnName, formatDate(rowData.getDate(column)));
                        }
                        break;
                    case OBJECT:
                        if (rowData.isNullLink(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            jsonObject.addProperty(columnName, rowData.getLink(column));
                        }
                        break;
                    case LIST:
                        jsonObject.addProperty(columnName, formatList(rowData.getLinkList(column)));
                        break;
                    case INTEGER_LIST:
                    case BOOLEAN_LIST:
                    case DOUBLE_LIST:
                    case STRING_LIST:
                    case BINARY_LIST:
                    case DATE_LIST:
                    case FLOAT_LIST:
                        if (rowData.isNullLink(column)) {
                            jsonObject.addProperty(columnName, nullValue);
                        } else {
                            RealmFieldType columnType = table.getColumnType(column);
                            jsonObject.addProperty(columnName, formatValueList(rowData.getValueList(column, columnType), columnType));
                        }
                        break;
                    default:
                        jsonObject.addProperty(columnName, "unknown column type: " + rowData.getColumnType(column));
                        break;
                }
            }
            jsonArray.add(jsonObject);
        }

        return jsonArray;
    }

    private String formatDate(Date date) {
        return dateTimeFormatter.format(date) + " (" + date.getTime() + ')';
    }

    private String formatList(OsList linkList) {
        final StringBuilder sb = new StringBuilder(linkList.getTargetTable().getName());
        sb.append("{");

        final long size = linkList.size();
        for (long pos = 0; pos < size; pos++) {
            if (pos != 0) {
                sb.append(',');
            }
            sb.append(linkList.getUncheckedRow(pos).getObjectKey());
        }

        sb.append("}");
        return sb.toString();
    }

    private String formatValueList(OsList linkList, RealmFieldType columnType) {
        final StringBuilder sb = new StringBuilder(columnType.name());
        sb.append("{");

        final long size = linkList.size();
        for (long pos = 0; pos < size; pos++) {
            if(pos != 0) {
                sb.append(',');
            }
            sb.append(linkList.getValue(pos));
        }

        sb.append("}");
        return sb.toString();
    }
}
