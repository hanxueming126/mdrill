/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.search.function;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Scorer;

import java.io.IOException;

/** @lucene.internal */
public class ValueSourceScorer extends Scorer {
  protected IndexReader reader;
  private int doc = -1;
  protected final int maxDoc;
  protected final DocValues values;
  protected boolean checkDeletes;

  protected ValueSourceScorer(IndexReader reader, DocValues values) {
    super(null, null);
    this.reader = reader;
    this.maxDoc = reader.maxDoc();
    this.values = values;
    setCheckDeletes(true);
  }

  public IndexReader getReader() {
    return reader;
  }

  public void setCheckDeletes(boolean checkDeletes) {
    this.checkDeletes = checkDeletes && reader.hasDeletions();
  }

  public boolean matches(int doc) {
    return (!checkDeletes || !reader.isDeleted(doc)) && matchesValue(doc);
  }

  public boolean matchesValue(int doc) {
    return true;
  }

  @Override
  public int docID() {
    return doc;
  }

  @Override
  public int nextDoc() throws IOException {
    for (; ;) {
      doc++;
      if (doc >= maxDoc) return doc = NO_MORE_DOCS;
      if (matches(doc)) return doc;
    }
  }

  @Override
  public int advance(int target) throws IOException {
    // also works fine when target==NO_MORE_DOCS
    doc = target - 1;
    return nextDoc();
  }

  public int doc() {
    return doc;
  }

  public boolean next() {
    for (; ;) {
      doc++;
      if (doc >= maxDoc) return false;
      if (matches(doc)) return true;
    }
  }

  public boolean skipTo(int target) {
    doc = target - 1;
    return next();
  }


  @Override
  public float score() throws IOException {
    return values.floatVal(doc);
  }

  public Explanation explain(int doc) throws IOException {
    return values.explain(doc);
  }
}

