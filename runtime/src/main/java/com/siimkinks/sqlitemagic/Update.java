package com.siimkinks.sqlitemagic;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.siimkinks.sqlitemagic.Select.Select1;

import java.util.ArrayList;

/**
 * Builder for SQL UPDATE statement.
 */
public final class Update extends UpdateSqlNode {
  Update() {
    super(null);
  }

  @Override
  protected void appendSql(@NonNull StringBuilder sb) {
    sb.append("UPDATE");
  }

  /**
   * Create a new builder for SQL UPDATE statement with conflict algorithm.
   *
   * @param conflictAlgorithm Conflict resolution algorithm to use
   * @return A new builder for SQL UPDATE statement
   */
  @CheckResult
  public static UpdateConflictAlgorithm withConflictAlgorithm(@ConflictAlgorithm int conflictAlgorithm) {
    return new UpdateConflictAlgorithm(new Update(), conflictAlgorithm);
  }

  /**
   * Create a new builder for SQL UPDATE statement.
   * <p>
   * Note that {@code table} param must be one of annotation processor generated table
   * objects that corresponds to table in a database.<br>
   * Example:
   * <pre>{@code
   * import static com.example.model.AuthorTable.AUTHOR;
   *
   * // [...]
   *
   * Update.table(AUTHOR)...
   * }</pre>
   *
   * @param table Table to update. This param must be one of annotation processor
   *              generated table objects that corresponds to table in a database
   * @param <T>   Table object type
   * @return A new builder for SQL UPDATE statement
   */
  @CheckResult
  public static <T> TableNode<T> table(@NonNull Table<T> table) {
    return new TableNode<>(new Update(), table);
  }

  /**
   * Builder for SQL UPDATE statement.
   */
  public static final class UpdateConflictAlgorithm extends UpdateSqlNode {
    @ConflictAlgorithm
    private final int conflictAlgorithm;

    UpdateConflictAlgorithm(@NonNull Update parent, @ConflictAlgorithm int conflictAlgorithm) {
      super(parent);
      this.conflictAlgorithm = conflictAlgorithm;
    }

    @Override
    protected void appendSql(@NonNull StringBuilder sb) {
      sb.append(ConflictAlgorithm.CONFLICT_VALUES[conflictAlgorithm]);
    }

    /**
     * Define a table to update.
     * <p>
     * Note that {@code table} param must be one of annotation processor generated table
     * objects that corresponds to table in a database.<br>
     * Example:
     * <pre>{@code
     * import static com.example.model.AuthorTable.AUTHOR;
     *
     * // [...]
     *
     * Update.table(AUTHOR)...
     * }</pre>
     *
     * @param table Table to update. This param must be one of annotation processor
     *              generated table objects that corresponds to table in a database
     * @param <T>   Table object type
     * @return A new builder for SQL UPDATE statement
     */
    @CheckResult
    public <T> TableNode<T> table(@NonNull Table<T> table) {
      return new TableNode<>(this, table);
    }
  }

  /**
   * Builder for SQL UPDATE statement.
   *
   * @param <T> Updated table object type
   */
  public static final class TableNode<T> extends UpdateSqlNode {
    @NonNull
    final Table<T> table;

    TableNode(@NonNull UpdateSqlNode parent, @NonNull Table<T> table) {
      super(parent);
      this.table = table;
      updateBuilder.tableNode = this;
    }

    @Override
    protected void appendSql(@NonNull StringBuilder sb) {
      table.appendToSqlFromClause(sb);
    }

    /**
     * Update a column with new value.
     *
     * @param column Column to update. This param must be one of annotation processor
     *               generated column objects that corresponds to column in a database
     *               table
     * @param value  A new value to set for updated column
     * @param <V>    Value type
     * @param <R>    Column return type
     * @param <ET>   Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull Column<V, R, ET, T> column, @NonNull V value) {
      return new Set<>(this, new UpdateColumn<>(column).is(value));
    }

    /**
     * Update a complex column with new value.
     *
     * @param column Column to update. This param must be one of annotation processor
     *               generated complex column objects that corresponds to a complex column
     *               in a database table
     * @param value  A new value to set for updated column
     * @param <V>    Value type
     * @param <R>    Column return type
     * @param <ET>   Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull ComplexColumn<V, R, ET, T> column, long value) {
      return new Set<>(this, new UpdateColumn<>(column).is(value));
    }

    /**
     * Update a column with the value of another column.
     *
     * @param column           Column to update. This param must be one of annotation processor
     *                         generated column objects that corresponds to column in a database
     *                         table
     * @param assignmentColumn Column who's value to assign to {@code column}.
     *                         This param must be one of annotation processor
     *                         generated column objects that corresponds to column in a database
     *                         table
     * @param <V>              Column value type
     * @param <R>              Column return type
     * @param <ET>             Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull Column<V, R, ET, T> column,
                                 @NonNull Column<?, ?, ? extends ET, ?> assignmentColumn) {
      return new Set<>(this, new UpdateColumn<>(column).is(assignmentColumn));
    }

    /**
     * Update a column with the value of inner SELECT statement result.
     *
     * @param column Column to update. This param must be one of annotation processor
     *               generated column objects that corresponds to column in a database
     *               table
     * @param select Inner select statement who's result will be assigned to {@code column}.
     *               Select statement must return 1x1 result set.
     * @param <V>    Column value type
     * @param <R>    Column return type
     * @param <ET>   Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull Column<V, R, ET, T> column,
                                 @NonNull SelectSqlNode.SelectNode<? extends ET, Select1> select) {
      return new Set<>(this, new UpdateColumn<>(column).is(select));
    }
  }

  private static final class UpdateColumn<T, R, ET, P> extends ComplexColumn<T, R, ET, P> {
    @NonNull
    private final Column<T, R, ET, P> parentColumn;

    UpdateColumn(@NonNull Column<T, R, ET, P> parentColumn) {
      super(parentColumn.table, parentColumn.name, parentColumn.allFromTable, parentColumn.valueParser, parentColumn.nullable, parentColumn.alias);
      this.parentColumn = parentColumn;
    }

    @NonNull
    @Override
    String toSqlArg(@NonNull T val) {
      return parentColumn.toSqlArg(val);
    }

    @Override
    void appendSql(@NonNull StringBuilder sb) {
      sb.append(name);
    }
  }

  /**
   * Builder for SQL UPDATE statement.
   *
   * @param <T> Updated table object type
   */
  public static final class Set<T> extends ExecutableNode {
    private final ArrayList<Expr> updates = new ArrayList<>(1);

    Set(@NonNull UpdateSqlNode parent, @NonNull Expr firstUpdate) {
      super(parent);
      updates.add(firstUpdate);
      firstUpdate.addArgs(updateBuilder.args);
    }

    @Override
    protected void appendSql(@NonNull StringBuilder sb) {
      sb.append("SET ");
      final ArrayList<Expr> updates = this.updates;
      int size = updates.size();
      for (int i = 0; i < size; i++) {
        if (i != 0) {
          sb.append(',');
        }
        updates.get(i).appendToSql(sb);
      }
    }

    /**
     * Update a column with new value.
     *
     * @param column Column to update. This param must be one of annotation processor
     *               generated column objects that corresponds to column in a database
     *               table
     * @param value  A new value to set for updated column
     * @param <V>    Value type
     * @param <R>    Column return type
     * @param <ET>   Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull Column<V, R, ET, T> column, @NonNull V value) {
      final Expr expr = new UpdateColumn<>(column).is(value);
      updates.add(expr);
      expr.addArgs(updateBuilder.args);
      return this;
    }

    /**
     * Update a complex column with new value.
     *
     * @param column Column to update. This param must be one of annotation processor
     *               generated complex column objects that corresponds to a complex column
     *               in a database table
     * @param value  A new value to set for updated column
     * @param <V>    Value type
     * @param <R>    Column return type
     * @param <ET>   Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull ComplexColumn<V, R, ET, T> column, long value) {
      final Expr expr = new UpdateColumn<>(column).is(value);
      updates.add(expr);
      expr.addArgs(updateBuilder.args);
      return this;
    }

    /**
     * Update a column with the value of another column.
     *
     * @param column           Column to update. This param must be one of annotation processor
     *                         generated column objects that corresponds to column in a database
     *                         table
     * @param assignmentColumn Column who's value to assign to {@code column}.
     *                         This param must be one of annotation processor
     *                         generated column objects that corresponds to column in a database
     *                         table
     * @param <V>              Column value type
     * @param <R>              Column return type
     * @param <ET>             Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull Column<V, R, ET, T> column,
                                 @NonNull Column<?, ?, ? extends ET, ?> assignmentColumn) {
      final Expr expr = new UpdateColumn<>(column).is(assignmentColumn);
      updates.add(expr);
      expr.addArgs(updateBuilder.args);
      return this;
    }

    /**
     * Update a column with the value of inner SELECT statement result.
     *
     * @param column Column to update. This param must be one of annotation processor
     *               generated column objects that corresponds to column in a database
     *               table
     * @param select Inner select statement who's result will be assigned to {@code column}.
     *               Select statement must return 1x1 result set.
     * @param <V>    Column value type
     * @param <R>    Column return type
     * @param <ET>   Column equivalent type
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public <V, R, ET> Set<T> set(@NonNull Column<V, R, ET, T> column,
                                 @NonNull SelectSqlNode.SelectNode<? extends ET, Select1> select) {
      final Expr expr = new UpdateColumn<>(column).is(select);
      updates.add(expr);
      expr.addArgs(updateBuilder.args);
      return this;
    }

    /**
     * Define SQL UPDATE statement WHERE clause.
     *
     * @param expr WHERE clause expression
     * @return SQL UPDATE statement builder
     */
    @CheckResult
    public Where where(@NonNull Expr expr) {
      return new Where(this, expr);
    }
  }

  /**
   * Builder for SQL UPDATE statement.
   */
  public static final class Where extends ExecutableNode {
    @NonNull
    private final Expr expr;

    Where(@NonNull UpdateSqlNode parent, @NonNull Expr expr) {
      super(parent);
      this.expr = expr;
      expr.addArgs(updateBuilder.args);
    }

    @Override
    protected void appendSql(@NonNull StringBuilder sb) {
      sb.append("WHERE ");
      expr.appendToSql(sb);
    }
  }
}
